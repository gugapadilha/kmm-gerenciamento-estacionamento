package com.example.gestodeestacionamento.presentation.viewmodel

import com.example.gestodeestacionamento.domain.model.User
import com.example.gestodeestacionamento.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk()
        viewModel = LoginViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `deve inicializar com estado vazio`() = runTest(testDispatcher) {
        val initialState = viewModel.uiState.value
        
        assertEquals("", initialState.email)
        assertEquals("", initialState.password)
        assertFalse(initialState.isLoading)
        assertNull(initialState.errorMessage)
        assertFalse(initialState.isLoginSuccessful)
    }

    @Test
    fun `deve atualizar email quando updateEmail é chamado`() = runTest(testDispatcher) {
        val email = "teste@example.com"
        
        viewModel.updateEmail(email)
        
        assertEquals(email, viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `deve atualizar senha quando updatePassword é chamado`() = runTest(testDispatcher) {
        val password = "senha123"
        
        viewModel.updatePassword(password)
        
        assertEquals(password, viewModel.uiState.value.password)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `deve mostrar erro quando email está vazio no login`() = runTest(testDispatcher) {
        viewModel.updateEmail("")
        viewModel.updatePassword("senha123")
        
        viewModel.login()
        advanceUntilIdle()
        
        assertEquals("Email e senha são obrigatórios", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isLoginSuccessful)
    }

    @Test
    fun `deve mostrar erro quando senha está vazia no login`() = runTest(testDispatcher) {
        viewModel.updateEmail("teste@example.com")
        viewModel.updatePassword("")
        
        viewModel.login()
        advanceUntilIdle()
        
        assertEquals("Email e senha são obrigatórios", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isLoginSuccessful)
    }

    @Test
    fun `deve fazer login com sucesso quando credenciais são válidas`() = runTest(testDispatcher) {
        val email = "teste@example.com"
        val password = "senha123"
        val user = User(
            id = 1L,
            email = email,
            name = "Teste",
            token = "token123",
            establishmentId = 1L,
            sessionId = 1L
        )
        
        coEvery { authRepository.login(email, password) } returns Result.success(user)
        
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        viewModel.login()
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value.isLoginSuccessful)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `deve mostrar erro quando login falha`() = runTest(testDispatcher) {
        val email = "teste@example.com"
        val password = "senha123"
        val errorMessage = "Credenciais inválidas"
        
        coEvery { authRepository.login(email, password) } returns Result.failure(Exception(errorMessage))
        
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        viewModel.login()
        advanceUntilIdle()
        
        assertEquals(errorMessage, viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isLoginSuccessful)
    }

    @Test
    fun `deve limpar erro quando clearError é chamado`() = runTest(testDispatcher) {
        viewModel.updateEmail("")
        viewModel.updatePassword("")
        viewModel.login()
        advanceUntilIdle()
        
        assertNotNull(viewModel.uiState.value.errorMessage)
        
        viewModel.clearError()
        
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `deve remover espaços em branco do email no login`() = runTest(testDispatcher) {
        val email = "  teste@example.com  "
        val password = "senha123"
        val user = User(
            id = 1L,
            email = email.trim(),
            name = "Teste",
            token = "token123",
            establishmentId = 1L,
            sessionId = 1L
        )
        
        coEvery { authRepository.login(email.trim(), password) } returns Result.success(user)
        
        viewModel.updateEmail(email)
        viewModel.updatePassword(password)
        viewModel.login()
        advanceUntilIdle()
        
        assertTrue(viewModel.uiState.value.isLoginSuccessful)
    }
}

