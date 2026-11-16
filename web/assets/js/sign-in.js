async function signIn() {
  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value.trim();
  const messageEl = document.getElementById("message");

  const credentials = { email, password };

  try {
    const response = await fetch("SignIn", {
      method: "POST",
      body: JSON.stringify(credentials),
      headers: { "Content-Type": "application/json" }
    });

    const json = await response.json();

    if (json.status) {
      //  redirect to home.html on success
      window.location.href = "index.html";
    } else {
      messageEl.innerText = json.message;
      messageEl.className = "text-danger";
    }
  } catch (error) {
    messageEl.innerText = "Sign-in failed. Please try again.";
    console.error("Sign-in error:", error);
  }
}
