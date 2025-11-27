package com.example.invyucab_project.domain.usecase

import com.example.invyucab_project.data.models.RiderOngoingRideResponse
import com.example.invyucab_project.data.repository.AppRepository
import retrofit2.Response
import javax.inject.Inject

class GetOngoingRideUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    suspend operator fun invoke(userId: Int): Response<RiderOngoingRideResponse> {
        return appRepository.getOngoingRideRiderSide(userId)
    }
}