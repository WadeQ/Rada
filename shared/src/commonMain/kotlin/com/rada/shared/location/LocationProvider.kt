import com.rada.shared.domain.DriverState
import kotlinx.coroutines.flow.Flow

interface LocationProvider {
    fun observeLocation(): Flow<DriverState>

    suspend fun getLastKnownLocation(): DriverState?
}