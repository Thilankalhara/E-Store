async function signOut() {
    try {
        // Include your web app context in the URL
        const response = await fetch("/WEB2_shop/SignOut");
        
        if (response.ok) {
            const json = await response.json();
            
            if (json.status) {
                window.location.href = "sign-in.html";
            } else {
                window.location.reload();
            }
        } else {
            console.log("Logout Failed");
        }
    } catch (error) {
        console.error("Error during sign out:", error);
    }
}

// Attach to logout button
document.addEventListener('DOMContentLoaded', function() {
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            signOut();
        });
    }
});
