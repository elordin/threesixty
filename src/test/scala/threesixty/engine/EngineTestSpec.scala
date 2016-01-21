package threesixty.engine

import org.scalatest.FunSpec

import threesixty.processor.Processor
import threesixty.visualizer.Visualizer
import threesixty.persistence.FakeDatabaseAdapter


class EngineTestSpec extends FunSpec {

    describe("A visualization engine") {
        describe("with not additional visualizations or processing methods") {
            describe("and the FakeDatabaseAdapter") {
                val engine = VisualizationEngine(new Processor {}, new Visualizer, FakeDatabaseAdapter)
                describe("when receiving a request with unknown type") {
                    val request = """{"type": "help", "for": "bullshit"}"""
                    it("should respond with an error") {
                        val response = engine.processRequest(request)
                        assertResult(response) {
                            ErrorResponse("""{ "error": "Unknown help-for parameter."}""")
                        }
                    }
                }

                describe("when receiving a help request without \"for\"") {
                    val request = """{"type": "help"}"""
                    it("should respond with its usage message") {
                        val response = engine.processRequest(request)
                        assertResult(response) { HelpResponse("""Threesixty - Visualization Engine

HELP
{                                       Returns usage information for modules of the engine.
    "type": "help",                     HELP_KEYWORD can be one of
    "for": HELP_KEYWORD                   - "visualizations"     Lists all available visualizations
}                                         - "processingmethods"  Lists all available processing methods
                                          - visualization_name   Shows arguments for this visualization
                                          - processing_method    Shows arguments for this processing method

VISUALIZATION
{                                       Returns a visualization as SVG
    "type": "visualization",
    "data": [ DATA_IDS ],               DATA_IDS is a list of IDs of the datasets for processing
    "visualization": {                  The "visualization" parameter is optional. If none is given it is deduced form the data.
        "type": VISUALIZATION_TYPE      VISUALIZATION_TYPE is the name of the visualization
        "args": VISUALIZATION_ARGS      VISUALIZATION_ARGS are the arguments of the visualization
    },
    "processor": [ PROCESSING_STEPS]    PROCESSING_STEPS is a list of processing steps
}                                       processing steps follow the format:
                                        {
                                            "method": "PROCESSING_METHOD",
                                            "args": PROCESSING_METHOD_ARGS
                                            "data": [ DATA_IDS ]
                                            "idmapping": [ ID_MAPPING ]
                                        }
                                        PROCESSING_METHOD is the name of the method.
                                        PROCESSING_METHOD_ARGS are the parameters for that method
                                        DATA_IDS is a list of IDs of datasets this method is applied to
                                        ID_MAPPING allows renaming the output dataset. Doing this allows
                                        to continue using both the original dataset unchanged and the
                                        processed one using their now different ids.
                                        If a dataset is processed whose ID is not in ID_MAPPING, it will
                                        be overridden with the processed result.
""")
                        }
                    }
                }
            }
        }
    }
}
