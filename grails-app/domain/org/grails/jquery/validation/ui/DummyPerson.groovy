package org.grails.jquery.validation.ui

class DummyPerson {
    String name
    String phone
    DummyAddress homeAddress
    DummyAddress workAddress
    static embedded = ['homeAddress', 'workAddress']

    static constraints = {
        name blank:false
        phone phone:true, minSize: 10
    }
}
