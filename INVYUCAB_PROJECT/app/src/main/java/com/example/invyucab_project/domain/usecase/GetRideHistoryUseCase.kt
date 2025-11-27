package com.example.invyucab_project.domain.usecase

import com.example.invyucab_project.data.models.RiderRideHistoryResponse
import com.example.invyucab_project.data.repository.AppRepository
import retrofit2.Response
import javax.inject.Inject

class GetRideHistoryUseCase @Inject constructor(
    private val appRepository: AppRepository
) {
    // ✅ UPDATED: Returns RiderRideHistoryResponse
    suspend operator fun invoke(userId: Int): Response<RiderRideHistoryResponse> {
        return appRepository.getRideHistory(userId)
    }
}