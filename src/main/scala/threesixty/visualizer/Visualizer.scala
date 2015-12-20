package threesixty.visualizer

import threesixty.data.ProcessedData
import threesixty.config.Config


case class Visualizer[V <: Visualization]()
    extends Function2[Set[ProcessedData], Config, V] {

    /**
     *  Visualizes the processed data.
     *  @param data Dataset to be viusalized
     *  @param config Configuration constraining the visualization
     *  @returns Visualization
     */
    def apply(data:Set[ProcessedData], config:Config):V =
        throw new NotImplementedError

    /**
     *  Alternative way of calling the visualizer.
     *  @see apply
     */
    // def process = apply(_,_)
    def visualize(data:Set[ProcessedData], config:Config) = apply(data, config)


}
