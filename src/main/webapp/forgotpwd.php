<?php
session_start();
require_once 'db_connection.php';

// Initialize database connection
$db = new Database();
$conn = $db->conn;

// Check if form is submitted
if ($_SERVER["REQUEST_METHOD"] == "POST") {
    // Sanitize inputs
    $username = filter_var($_POST['username'], FILTER_SANITIZE_STRING);
    $newPassword = $_POST['newPassword'];
    $confirmPassword = $_POST['confirmPassword'];
    
    // Validate input
    if (empty($username) || empty($newPassword) || empty($confirmPassword)) {
        header("Location: forgotpwd.html?error=" . urlencode("All fields are required"));
        exit();
    }
    
    // Check if passwords match
    if ($newPassword !== $confirmPassword) {
        header("Location: forgotpwd.html?error=" . urlencode("Passwords do not match"));
        exit();
    }
    
    // Check password length
    if (strlen($newPassword) < 8) {
        header("Location: forgotpwd.html?error=" . urlencode("Password must be at least 8 characters long"));
        exit();
    }
    
    try {
        // Check if username exists
        $stmt = $conn->prepare("SELECT id FROM users WHERE username = ?");
        $stmt->bind_param("s", $username);
        $stmt->execute();
        $result = $stmt->get_result();
        
        if ($result->num_rows === 0) {
            // Username not found
            header("Location: forgotpwd.html?error=" . urlencode("Username not found"));
            exit();
        }
        
        // Hash the new password
        $hashedPassword = password_hash($newPassword, PASSWORD_DEFAULT);
        
        // Update the password in database
        $updateStmt = $conn->prepare("UPDATE users SET password = ? WHERE username = ?");
        $updateStmt->bind_param("ss", $hashedPassword, $username);
        $updateStmt->execute();
        
        if ($updateStmt->affected_rows > 0) {
            // Password updated successfully
            header("Location: login.html?success=" . urlencode("Password has been reset successfully. You can now login."));
            exit();
        } else {
            // No rows were affected, might be an issue
            header("Location: forgotpwd.html?error=" . urlencode("Failed to update password. Please try again."));
            exit();
        }
        
    } catch (Exception $e) {
        // Log the error
        error_log("Password reset error: " . $e->getMessage());
        header("Location: forgotpwd.html?error=" . urlencode("An error occurred. Please try again later."));
        exit();
    }
    
} else {
    // If not POST request, redirect back to forgot password page
    header("Location: forgotpwd.html");
    exit();
}

// Close the database connection
$db->closeConnection();
?>