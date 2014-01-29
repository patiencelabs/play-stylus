package patience.assets.test

import org.scalatest.Spec
import java.io.File
import patience.assets.StylusCompiler
import play.PlayExceptions.AssetCompilationException

class StylusCompilerSpec extends Spec {

  describe("StylusCompiler") {

    it("should compile well-formed stylus file") {
      val stylFile = new File("test_cases/well_formed.styl")
      val (full, minified, file) = StylusCompiler.compile(stylFile, Nil)
      assert(full === ".test {\n  display: none;\n}\n\n")
      assert(minified.orNull === ".test{display:none}\n\n")
    }

    it("should compile stylus file with import") {
      val stylFile = new File("test_cases/with_import.styl")
      val (full, minified, file) = StylusCompiler.compile(stylFile, Nil)
      assert(full === ".test {\n  color: #008000;\n}\n\n")
      assert(minified.orNull === ".test{color:#008000}\n\n")
    }

    it("should include plain css import") {
      val stylFile = new File("test_cases/with_css_import.styl")
      val (full, minified, file) = StylusCompiler.compile(stylFile, "--include-css" :: Nil)
      assert(full === ".test {\n  color: #008000;\n}\nbody { display: none; }\n\n")
      assert(minified.orNull === ".test{color:#008000}\nbody { display: none; }\n\n")
    }

    it("should parse error for ill-formed stylus file") {
      val stylFile = new File("test_cases/ill_formed.styl")
      val thrown = intercept[play.PlayExceptions.AssetCompilationException] {
        StylusCompiler.compile(stylFile, Nil)
      }
      val expectedMessage =
        """Compilation error[Stylus compiler: illegal unary "in", missing left-hand operand]"""
      assert(thrown.getMessage === expectedMessage)
      assert(thrown.line === 2)
    }

  }

}
