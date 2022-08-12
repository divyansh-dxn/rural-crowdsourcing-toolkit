package com.microsoft.research.karya.ui.homeScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.microsoft.research.karya.data.manager.AuthManager
import com.microsoft.research.karya.data.model.karya.WorkerRecord
import com.microsoft.research.karya.data.model.karya.modelsExtra.EarningStatus
import com.microsoft.research.karya.data.model.karya.modelsExtra.TaskStatus
import com.microsoft.research.karya.data.repo.AssignmentRepository
import com.microsoft.research.karya.data.repo.PaymentRepository
import com.microsoft.research.karya.data.repo.TaskRepository
import com.microsoft.research.karya.data.repo.WorkerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel
@Inject
constructor(
  private val taskRepository: TaskRepository,
  private val assignmentRepository: AssignmentRepository,
  private val authManager: AuthManager,
  private val workerRepository: WorkerRepository,
  private val paymentRepository: PaymentRepository
) : ViewModel() {

  private lateinit var worker: WorkerRecord

  // Worker details
  private var _name = MutableStateFlow("")
  val name = _name.asStateFlow()

  private var _phoneNumber = MutableStateFlow("")
  val phoneNumber = _phoneNumber.asStateFlow()

  // XP points
  private var _points = MutableStateFlow(0)
  val points = _points.asStateFlow()

  // Task summary
  private var _taskSummary = MutableStateFlow(
    TaskStatus(
      0,
      0,
      0,
      0,
      0,
      0
    )
  )
  val taskSummary = _taskSummary.asStateFlow()

  // Earnings summary
  private var _earningStatus = MutableStateFlow(EarningStatus(0, 0, 0))
  val earningStatus = _earningStatus.asStateFlow()

  init {
    refreshWorker()
    refreshXPPoints()
    refreshTaskSummary()
    refreshEarningSummary()
  }

  private fun refreshWorker() {
    viewModelScope.launch {
      worker = authManager.getLoggedInWorker()
      val name = try {
        worker.profile!!.asJsonObject.get("name").asString
      } catch (e: Exception) {
        "No Name"
      }

      val phoneNumber = try {
        worker.phoneNumber!!
      } catch (e: Exception) {
        "9999999999"
      }

      _name.value = name
      _phoneNumber.value = phoneNumber
    }
  }

  fun refreshXPPoints() {
    if (this::worker.isInitialized) {
      viewModelScope.launch {
        _points.value = try {
          workerRepository.getXPPoints(worker.id)!!
        } catch (e: Exception) {
          0
        }
      }
    }
  }

  fun refreshTaskSummary() {
    viewModelScope.launch {
      _taskSummary.value = taskRepository.getTaskSummary()
    }
  }

  fun refreshEarningSummary() {
    viewModelScope.launch {
      _earningStatus.value = EarningStatus(0, 0, 0)
    }
  }
}