package testing

class Simple {
  def foo() {}
}

object SimpleSpec {

  def describe(description:String)(spec: => Unit) {}
  def it(description:String)(spec: => Unit) {}

  def start() {

    var simple:Simple = new Simple

    describe("when foo is called") {

      it("should return a message") {
        println("foo")
        //expect(simple.foo).toEqual("a message")
      }
    }
  }
}

