document.addEventListener("DOMContentLoaded", async function () {
  try {
    // Load profile data from server
    const res = await fetch("/edit-profile");
    if (!res.ok) throw new Error("Failed to load profile");

    const data = await res.json();

    // Map the response data to form fields using exact snake_case names
    document.querySelector("input[name='first_name']").value = data.first_name || "";
    document.querySelector("input[name='last_name']").value = data.last_name || "";
    document.querySelector("input[name='mobile']").value = data.mobile || "";
    document.querySelector("input[name='email']").value = data.email || "";
    document.querySelector("input[name='line_1']").value = data.line_1 || "";
    document.querySelector("input[name='line_2']").value = data.line_2 || "";
    document.querySelector("input[name='postal_code']").value = data.postal_code || "";

    // Update profile card text (name and email)
    const fullName = ((data.first_name || "") + " " + (data.last_name || "")).trim();
    document.querySelector(".glass-card h5").textContent = fullName || "User";
    document.querySelector(".glass-card p").textContent = data.email || "";

  } catch (error) {
    console.error("Error loading profile:", error);
  }

  // Handle form submit
  document.querySelector("form").addEventListener("submit", async function (e) {
    e.preventDefault();

    const payload = {
      first_name: document.querySelector("input[name='first_name']").value,
      last_name: document.querySelector("input[name='last_name']").value,
      mobile: document.querySelector("input[name='mobile']").value,
      email: document.querySelector("input[name='email']").value,
      line_1: document.querySelector("input[name='line_1']").value,
      line_2: document.querySelector("input[name='line_2']").value,
      postal_code: document.querySelector("input[name='postal_code']").value,
      old_password: document.querySelector("input[name='old_password']").value,
      new_password: document.querySelector("input[name='new_password']").value,
      confirm_password: document.querySelector("input[name='confirm_password']").value,
    };

    try {
      const response = await fetch("/edit-profile", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      const result = await response.json();

      if (result.redirect) {
        window.location.href = result.redirect;
      } else if (result.success) {
        alert("Profile updated successfully!");
        location.reload();
      } else {
        alert(result.message || "Update failed");
      }
    } catch (error) {
      console.error("Error updating profile:", error);
    }
  });
});
