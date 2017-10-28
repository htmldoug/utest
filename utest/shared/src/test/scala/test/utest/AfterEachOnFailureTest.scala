package test.utest

import utest._
import utest.framework.ExecutionContext.RunNow

import scala.concurrent.Future
/**
 * Put executor.utestAfterEach(path) into finally block to make sure it will be executed regardless of the test failing.
 */
object AfterEachOnFailureTest extends TestSuite {

  private var res:SomeResource = _

  override def utestBeforeEach(path: Seq[String]): Unit = {
    res = new SomeResource //open resource
  }

  override def utestAfterEach(path: Seq[String]): Unit = {
    res.close()
  }

  override def utestAfterAll(): Unit = {
    println(s"Resource closed? ${res.isClosed}")
    assert(res.isClosed)
  }

  val tests = Tests{
    'hello{
      Future(0)
    }
    'testFails {
      val innerTests = Tests{
        throw new java.lang.AssertionError("Fail")
      }
      TestRunner.runAsync(innerTests, executor = this).map { results =>
        val leafResults = results.leaves.toSeq
        assert(leafResults(0).value.isFailure)
        leafResults
      }
    }
  }



  private class SomeResource extends AutoCloseable{
    var isClosed:Boolean = false
    override def close(): Unit = isClosed = true
  }
}
