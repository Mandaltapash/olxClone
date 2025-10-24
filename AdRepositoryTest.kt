package com.tods.project_olx

import com.google.firebase.database.DatabaseReference // Import DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference // Import StorageReference
import com.tods.project_olx.data.local.AdDao
import com.tods.project_olx.data.local.AdEntity
import com.tods.project_olx.data.repository.AdRepository
import com.tods.project_olx.model.Ad
import com.tods.project_olx.utils.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class AdRepositoryTest {

    // Rule for LiveData testing
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    // Use StandardTestDispatcher for more control if needed
    private val testDispatcher = UnconfinedTestDispatcher(TestCoroutineScheduler())

    // --- Mocks ---
    @Mock
    private lateinit var firebaseDatabaseMock: FirebaseDatabase
    @Mock
    private lateinit var firebaseStorageMock: FirebaseStorage
    @Mock
    private lateinit var adDaoMock: AdDao
    @Mock
    private lateinit var adsRefMock: DatabaseReference // Mock DatabaseReference for "ads"
    @Mock
    private lateinit var myAdsRefMock: DatabaseReference // Mock DatabaseReference for "my_adds"
    @Mock
    private lateinit var storageRefMock: StorageReference // Mock root StorageReference
    @Mock
    private lateinit var adImageRefMock: StorageReference // Mock specific image StorageReference

    // --- Class Under Test ---
    private lateinit var repository: AdRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this) // Initialize mocks
        Dispatchers.setMain(testDispatcher) // Set main dispatcher for testing Coroutines

        // Mock Firebase behavior: database.getReference("...") returns mocked refs
        whenever(firebaseDatabaseMock.getReference("ads")).thenReturn(adsRefMock)
        whenever(firebaseDatabaseMock.getReference("my_adds")).thenReturn(myAdsRefMock)
        // Mock storage behavior: storage.reference returns mocked root ref
        whenever(firebaseStorageMock.reference).thenReturn(storageRefMock)
        // Mock storage path behavior: rootRef.child(...) returns mocked ad image ref
        whenever(storageRefMock.child(anyString())).thenReturn(adImageRefMock) // Mocking child path

        // --- Initialize Repository with Mocks ---
        // Pass the DatabaseReference and StorageReference mocks, not the root objects
        repository = AdRepository(adsRefMock, myAdsRefMock, storageRefMock, adDaoMock)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // Reset main dispatcher after tests
    }

    // --- Test Cases ---

    @Test
    fun `getAdsFlow should return success when local data exists`() = runTest(testDispatcher) {
        // Given: Mock DAO response
        val mockDbAds = listOf(
            AdEntity("db1", "State1", "Cat1", "DB Ad 1", "Desc 1", "100", "123", "img1.jpg", System.currentTimeMillis()),
            AdEntity("db2", "State2", "Cat2", "DB Ad 2", "Desc 2", "200", "456", "img2.jpg", System.currentTimeMillis())
        )
        val mockDbFlow = flowOf(mockDbAds)
        whenever(adDaoMock.getAllAds()).thenReturn(mockDbFlow)

        // Mock Firebase listener to simulate no new data initially
        doAnswer { invocation ->
            val listener = invocation.getArgument<ValueEventListener>(0)
            // Simulate Firebase returning an empty snapshot initially
            val snapshotMock: DataSnapshot = mock()
            whenever(snapshotMock.exists()).thenReturn(false)
            listener.onDataChange(snapshotMock)
            null // Explicitly return null for void methods
        }.`when`(adsRefMock).addValueEventListener(any<ValueEventListener>())


        // When: Call the repository function and collect the first emission
        val result = repository.getAdsFlow(null, null).first() // Collect first result

        // Then: Assert the result is Success and contains data from DAO
        assertTrue(result is Resource.Success)
        assertNotNull(result.data)
        // Assuming your AdEntity.toAd() mapper exists and works
        // assertEquals(2, result.data?.size)
        // assertEquals("DB Ad 1", result.data?.get(0)?.title)

        // Verify: Check if DAO method was called
        verify(adDaoMock).getAllAds()
        // Verify Firebase listener was attached
        verify(adsRefMock).addValueEventListener(any<ValueEventListener>())
    }

    @Test
    fun `saveAd should insert into local DB and Firebase`() = runTest(testDispatcher) {
        // Given
        val adToSave = Ad(id = "new1", title = "New Ad", state = "State", category = "Cat", value = "50", phone = "789", description = "New Desc")
        val adEntity = adToSave.toEntity() // Assuming Ad.toEntity() mapper exists

        // Mock Firebase push() and setValue() Task to return success
        val pushRefMock: DatabaseReference = mock()
        val setValueTask: Task<Void> = mock()
        whenever(adsRefMock.child(anyString()).child(anyString())).thenReturn(pushRefMock) // Mock path for state/category
        whenever(pushRefMock.push()).thenReturn(pushRefMock) // Mock push()
        whenever(pushRefMock.key).thenReturn(adToSave.id) // Return the ID when key is requested
        whenever(pushRefMock.setValue(any())).thenReturn(setValueTask) // Mock setValue()
        // Simulate successful Task
        doAnswer { invocation ->
            invocation.getArgument<com.google.android.gms.tasks.OnSuccessListener<Void>>(0).onSuccess(null)
            setValueTask // Return the mock Task itself
        }.`when`(setValueTask).addOnSuccessListener(any())
        doReturn(setValueTask).`when`(setValueTask).addOnFailureListener(any()) // Make failure listener do nothing for success case


        // When
        val result = repository.saveAd(adToSave)

        // Then
        assertTrue(result is Resource.Success)

        // Verify: Check if DAO insert and Firebase setValue were called
        // Use timeout(1000) for suspend functions if using StandardTestDispatcher
        verify(adDaoMock /*, timeout(1000)*/).insertAd(adEntity)
        verify(adsRefMock.child(adToSave.state).child(adToSave.category).child(adToSave.id))
            .setValue(adToSave.copy(id = "", adImages = emptyList())) // Verify correct object saved to Firebase
        // Add verification for myAdsRefMock if saveAd also saves there
    }

    // Add more tests for other scenarios:
    // - getAdsFlow with filters (region, category)
    // - getAdsFlow when Firebase returns data
    // - getAdsFlow when Firebase returns error
    // - saveAd failure (Firebase error)
    // - deleteAd success and failure (Firebase and local)
    // - searchAds functionality
    // - uploadImages functionality (this is harder to unit test, might need integration test)
}

// Helper extension function if needed (assuming Ad has toEntity)
fun Ad.toEntity(): AdEntity {
    return AdEntity(
        id = this.id,
        state = this.state,
        category = this.category,
        title = this.title,
        description = this.description,
        value = this.value,
        phone = this.phone,
        images = this.adImages.joinToString(","), // Simple comma separation for example
        timestamp = System.currentTimeMillis() // Or handle timestamp appropriately
    )
}

