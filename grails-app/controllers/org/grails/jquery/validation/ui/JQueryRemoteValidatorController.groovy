/* Copyright 2010-2012 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.grails.jquery.validation.ui

import org.codehaus.groovy.grails.validation.ConstrainedPropertyBuilder
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.validation.BeanPropertyBindingResult

/**
*
* @author <a href='mailto:limcheekin@vobject.com'>Lim Chee Kin</a>
*
* @since 0.1
*/
class JQueryRemoteValidatorController {
	
	def jqueryValidationService
    def messageSource

	def validate(){
        log.debug ("Received following params to validate action: " + params.toString())
		def validatableClass = grailsApplication.classLoader.loadClass(params.validatableClass)
        log.debug ("Loaded via grailsApplication.classLoader validateableClass:  " + validatableClass)
		def constrainedProperties = jqueryValidationService.getConstrainedProperties(validatableClass)
        log.debug ("Retrieved constrainedProperties from validatableClass: " + constrainedProperties)

		def validatableInstance
        /*
        If the check is for a Domain Class versus a Command Object then we can simply do:
        grailsApplication.isDomainClass(validatableClass.getClass()) and then read
        in an object based on the passed in params.id
         */
		if (!params.id || params.id.equals("0")) {
			validatableInstance = validatableClass.newInstance()
            log.debug ("Created a newInstance of validatableClass and stored in validatableInstance " + validatableInstance)
		} else {
			validatableInstance = validatableClass.read(params.id.toLong())
            log.debug ("Retrieved validatableClass from the database via read() method: " + validatableInstance)
		}

        applicationContext.autowireCapableBeanFactory.autowireBeanProperties(validatableInstance,
                AutowireCapableBeanFactory.AUTOWIRE_BY_NAME, false)
        log.debug ("Autowired bean references for dependency injection support: " + validatableInstance)

		def errors = new BeanPropertyBindingResult(validatableInstance, validatableInstance.class.name)
        log.debug ("Errors created by BeanPropertyBindingResult: " + errors)

		def constrainedProperty = constrainedProperties[params.property]
        log.debug ("Retrieved the constrainedProperty: $constrainedProperty using params.property entry: ${params.property} ")

		constrainedProperty.messageSource = messageSource
        log.debug ("Set constrainedProperty messageSource to: " + constrainedProperty.messageSource)

		Object propertyValue 
		if (constrainedProperty.propertyType == String) {
			propertyValue = params[params.property]
            log.debug ("constrainedProperty.propertyType is String retrieving string value from params: $propertyValue" )
		} else {
            bindData(validatableInstance, params, [include: [params.property]])
            log.debug("bindData from params.property ${params.property} to validatableInstance")
	        propertyValue = validatableInstance."${params.property}"
            log.debug ("propertyValue after bindData: $propertyValue class: ${propertyValue.class.simpleName}")
		}
		
		constrainedProperty.validate(validatableInstance, propertyValue, errors)
        log.debug ("After validate constrainedProperty errors object: $errors" )

        if(validatableInstance.metaClass.respondsTo(validatableInstance, "isAttached") &&
                validatableInstance.isAttached()){
            validatableInstance.discard()
            log.debug ("Discarded validatableInstance since it was a domain class")
        }

		def fieldError = errors.getFieldError(params.property)

        log.debug  ("fieldError = ${fieldError}")
        log.debug  ("fieldError.code = ${fieldError?.code}")
        log.debug  ("params.constraint = ${params.constraint}")
        log.debug  ("params.property = ${params.property}")

		response.setContentType("text/json;charset=UTF-8")

		if (fieldError && fieldError.code.indexOf(params.constraint) > -1) {
			// if constraint is known then render false (use default message), 
			// otherwise render custom message.
			render params.constraint ? "false" : """{"message":"${message(error: errors.getFieldError(params.property))}"}"""
		} else {
			render "true"
		}
	}
}
