package org.grails.jquery.validation.ui
import grails.validation.Validateable
/**
 * Created with IntelliJ IDEA.
 * User: christopher
 * Date: 1/7/13
 * Time: 8:45 AM
 * To change this template use File | Settings | File Templates.
 */
@Validateable
class ProcessElementCommand {
    String name

    static constraints = {
        name(blank: false)
    }
}
