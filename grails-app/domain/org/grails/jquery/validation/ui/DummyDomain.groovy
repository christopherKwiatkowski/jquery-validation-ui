package org.grails.jquery.validation.ui

class DummyDomain {
    def dummyService

    String title
    String validateMe
    String validateUnknownMessage

    static constraints = {
        title(blank: false)
        validateMe(blank: false, maxSize: 10, validator: { val, obj ->
            return obj.dummyService(val.equals("valid"))
        })
        validateUnknownMessage(blank: false, maxSize: 10, validator: { val, obj ->
            obj.dummyService(val.equals("valid")) ?: "validateUnknownMessage.invalid"
        })
    }
}
