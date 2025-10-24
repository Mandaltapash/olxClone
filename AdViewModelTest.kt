package com.tods.project_olx

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tods.project_olx.data.local.AdDao
import com.tods.project_olx.data.repository.AdRepository
import com.tods.project_olx.model.Ad // Import Ad model
import com.tods.project_olx.presentation.viewmodel.AdViewModel // Import correct ViewModel
import com.tods.project_olx.utils.Resource
import com.tods.project_olx.utils.Validator
import com.tods.project_olx.utils.ValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class AdViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Use StandardTestDispatcher for more control if needed, UnconfinedTestDispatcher runs eagerly
    private val testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())

    @Mock
    private lateinit var repository: AdRepository

    private lateinit var viewModel: AdViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher) // Set main dispatcher for testing
        viewModel = AdViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset main dispatcher after test
    }

    @Test
    fun `fetchAds should update ads LiveData on success`() = runTest(testDispatcher) { // Run test with the test dispatcher
        // Given
        val mockAds = listOf(
            Ad(id = "1", title = "Test Ad 1", description = "Description 1"),
            Ad(id = "2", title = "Test Ad 2", description = "Description 2")
        )
        // Simulate repository returning a Flow wrapped in Resource.Success
        `when`(repository.getAdsFlow(null, null)).thenReturn(flowOf(Resource.Success(mockAds)))

        // When
        viewModel.fetchAds() // Call the function to test
        // advanceUntilIdle() // Use if using StandardTestDispatcher to run pending coroutines

        // Then
        val result = viewModel.ads.value // Observe the LiveData
        assertNotNull(result)
        assertTrue(result is Resource.Success)
        assertEquals(2, result.data?.size)
        // Verify repository interaction
        verify(repository).getAdsFlow(null, null)
    }

    @Test
    fun `fetchAds should update error LiveData on failure`() = runTest(testDispatcher) {
        // Given
        val errorMessage = "Network error"
        // Simulate repository returning a Flow wrapped in Resource.Error
        `when`(repository.getAdsFlow(null, null)).thenReturn(flowOf(Resource.Error(errorMessage)))

        // When
        viewModel.fetchAds()

        // Then
        val result = viewModel.ads.value
        assertNotNull(result)
        assertTrue(result is Resource.Error)
        assertEquals(errorMessage, result.message)
        verify(repository).getAdsFlow(null, null)
    }

    @Test
    fun `saveAd should call repository saveAd`() = runTest(testDispatcher) {
        // Given
        val ad = Ad(id = "1", title = "Test Ad")
        // Mock the suspend function
        `when`(repository.saveAd(ad)).thenReturn(Resource.Success(Unit)) // Assuming saveAd returns Resource

        // When
        viewModel.saveAd(ad)

        // Then
        // Verify that the suspend function was called
        verify(repository).saveAd(ad)
        // Optionally check LiveData state if saveAd updates any
        // assertNotNull(viewModel.saveStatus.value)
        // assertTrue(viewModel.saveStatus.value is Resource.Success)
    }
}

