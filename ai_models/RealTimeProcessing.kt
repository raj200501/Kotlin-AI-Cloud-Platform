package ai_models

import kotlinx.coroutines.*
import org.tensorflow.SavedModelBundle
import org.tensorflow.Tensor
import org.tensorflow.types.TFloat32

object RealTimeProcessing {
    private val model = SavedModelBundle.load("path/to/real_time_model", "serve")
    private val session = model.session()

    fun startProcessing(dataStream: List<FloatArray>) = runBlocking {
        dataStream.forEach { data ->
            launch {
                process(data)
            }
        }
    }

    private suspend fun process(data: FloatArray) {
        withContext(Dispatchers.Default) {
            val tensor = TFloat32.tensorOf(Tensor.shape(1, data.size.toLong()), Tensor.create(data))
            val result = session.runner()
                .feed("input_tensor", tensor)
                .fetch("output_tensor")
                .run()[0] as TFloat32
            println("Processed: ${result.data().getFloat(0)}")
            result.close()
            tensor.close()
        }
    }
}
