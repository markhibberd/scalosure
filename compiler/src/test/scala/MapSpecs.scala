package s2js

import org.scalatest.fixture.FixtureSpec
import org.scalatest.{ Spec, BeforeAndAfterAll }

class MapSpecs extends PrinterFixtureSpec {

    ignore("maps") {

        it("can have nested maps") {

            parser expect {"""
                object a {
                    val x = Map(
                        "name"->s2js.JsObject("first"->"bat", "last"->"man"), 
                        "title"->"pres",
                        "fast"->true,
                        "age"->5)
                }
            """} toBe {"""
                goog.provide('a');
                a.x = new scala.collection.mutable.HashMap({
                    'name':{'first':'bat','last':'man'},
                    'title':'pres',
                    'fast':true,
                    'age':5});
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
                a.x = new scala.collection.mutable.HashMap({
                    'name':'batman',
                    'powers':['fighting','IQ']});
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
                a.x = new scala.collection.mutable.HashMap({
                    'name':'batman',
                    'powers':[
                        {'name':'fighting','level':6},
                        {'name':'IQ','level':10}]});
            """}

        }

        it("can access map items by key") {

            parser expect {"""

            class A {
                var xs:Map[String,Any] = null

                def get(v1:String) = {
                    xs(v1)
                }
            }

            """} toBe {"""

            goog.provide('A');
            
            /** @constructor*/A = function() {var self = this;};
            A.prototype.xs = null;
            A.prototype.get = function(v1) {var self = this;return self.xs[v1];};

            """}
        }

        ignore("supports foreach method") {

            parser expect {"""

            object o {
                var xs = Map("1"->"foo", "2"->"bar")

                def m1() {
                    xs foreach {
                        x => println(x._1+"="+x._2)
                    }
                }
            }

            """} toBe {"""

            goog.provide('o');
            o.xs = {'1':'foo','2':'bar'};
            o.m1 = function() {
                var self = this;
                for(var _key_ in o.xs) {
                    (function(x) {
                        console.log(((x._1 + '=') + x._2));
                    })({_1:_key_, _2:o.xs[_key_]});
                };
            };

            """}
        }
    }
}

// vim: set ts=4 sw=4 et:
