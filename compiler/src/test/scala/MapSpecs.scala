package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class MapSpecs extends PrinterFixtureSpec {

    describe("maps") {

        it("can have nested maps") {

            parser expect {"""
                object a {
                    val x = Map(
                        "name"->Map("first"->"bat", "last"->"man"), 
                        "title"->"pres",
                        "fast"->true,
                        "age"->5)
                }
            """} toBe {"""
                goog.provide('a');
                a.x = {'name':{'first':'bat','last':'man'},
                       'title':'pres',
                       'fast':true,
                       'age':5};
            """}
        }
        
        it("can have arrays of strings") {

            parser expect {"""
                object a {
                    val x = Map(
                        "name"->"batman",
                        "powers"->Array("fighting", "IQ"))
                }
            """} toBe {"""
                goog.provide('a');
                goog.require('goog.array');
                a.x = {
                    'name':'batman',
                    'powers':['fighting','IQ']};
            """}
        }

        it("can have arrays of maps") {

            parser expect {"""
                object a {
                    val x = Map(
                        "name"->"batman",
                        "powers"->Array(
                            Map("name"->"fighting", "level"->6),
                            Map("name"->"IQ", "level"->10)))
                }
            """} toBe {"""
                goog.provide('a');
                goog.require('goog.array');
                a.x = {
                    'name':'batman',
                    'powers':[
                        {'name':'fighting','level':6},
                        {'name':'IQ','level':10}]};
            """}

        }
    }
}

// vim: set ts=4 sw=4 et:
