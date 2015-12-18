package threesixty.config

object Implicits {
    import spray.json._

    /**
     *  Implicit conversion to parse a JSON Object to Config
     */
    // since Config is a case class, easier conversions might be available.
    // Check the spray-json docs at https://github.com/spray/spray-json
    implicit object ConfigJsonFormat extends RootJsonFormat[Config] {

        def write(c: Config) = throw new NotImplementedError

        def read(value: JsValue) = throw new NotImplementedError

    }
}

/**
 *  Config potentially contains all options transmitted from the client.
 *  Some values may be required, some may be optional.
 */
case class Config() {

}

