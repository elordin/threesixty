package threesixty.config

import threesixty.processor._

object Implicits {
    import spray.json._

    // since Config is a case class, easier conversions are available
    implicit object ConfigJsonFormat extends RootJsonFormat[Config] {

        def write(c: Config) = throw new NotImplementedError

        def read(value: JsValue) = throw new NotImplementedError

    }
}

case class Config {

}

