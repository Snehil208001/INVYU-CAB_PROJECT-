package com.example.invyucab_project.domain.usecase

import com.example.invyucab_project.data.models.RiderOngoingRideResponse
import com.example.invyucab_project.data.repository.AppRepository
import retrofit2.Response
import javax.inject.Inject

class GetOngoingRideUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    // ✅ CHANGED: Accepts rideId instead of userId
    suspend operator fun invoke(rideId: Int): Response<RiderOngoingRideResponse> {
        return appRepository.getOngoingRideRiderSide(rideId)
    }
}