document.addEventListener('DOMContentLoaded', () => {
  loadProducts();

  const form = document.getElementById('productForm');
  form.addEventListener('submit', submitProduct);
});

function loadProducts() {
  fetch('productList')
    .then(res => res.json())
    .then(data => {
      const tbody = document.getElementById('productTableBody');
      tbody.innerHTML = '';
      data.products.forEach(p => {
        const row = document.createElement('tr');
        row.innerHTML = `
          <td>${p.name}</td>
          <td>${p.title}</td>
          <td>${p.price}</td>
          <td>${p.qty}</td>
          <td>${p.description}</td>
          <td>
            <img src="assets/store/${p.image1}" alt="" width="50">
            <img src="assets/store/${p.image2}" alt="" width="50">
          </td>
          <td>
            <button class="btn btn-sm btn-warning me-1" onclick="editProduct(${p.id})">Edit</button>
            <button class="btn btn-sm btn-danger" onclick="deleteProduct(${p.id})">Delete</button>
          </td>
        `;
        tbody.appendChild(row);
      });
    });
}

function submitProduct(e) {
  e.preventDefault();
  const submitBtn = document.getElementById('submitBtn');
  const loading = submitBtn.querySelector('.loading');
  submitBtn.disabled = true;
  loading.style.display = 'inline-block';

  const id = document.getElementById('productId').value;
  if (id) {
    // update
    const formData = new URLSearchParams();
    formData.append('id', id);
    formData.append('name', e.target.name.value);
    formData.append('title', e.target.title.value);
    formData.append('price', e.target.price.value);
    formData.append('qty', e.target.quantity.value);
    formData.append('description', e.target.description.value);

    fetch('productList', { method: 'POST', body: formData })
      .then(res => res.json())
      .then(data => {
        alert(data.message);
        if (data.success) {
          e.target.reset();
          document.getElementById('productId').value = '';
          document.getElementById('btnText').innerText = 'Add Product';
          loadProducts();
          resetFileInputsRequired();
        }
      })
      .finally(() => {
        submitBtn.disabled = false;
        loading.style.display = 'none';
      });
  } else {
    // insert
    const formData = new FormData(e.target);
    fetch('insertProduct', { method: 'POST', body: formData })
      .then(res => res.json())
      .then(data => {
        alert(data.message);
        if (data.success) {
          e.target.reset();
          loadProducts();
        }
      })
      .finally(() => {
        submitBtn.disabled = false;
        loading.style.display = 'none';
      });
  }
}

function editProduct(id) {
  fetch('productList')
    .then(res => res.json())
    .then(data => {
      const p = data.products.find(pr => pr.id === id);
      if (!p) return;

      document.querySelector('[name="name"]').value = p.name;
      document.querySelector('[name="title"]').value = p.title;
      document.querySelector('[name="price"]').value = p.price;
      document.querySelector('[name="quantity"]').value = p.qty;
      document.querySelector('[name="description"]').value = p.description;
      document.getElementById('productId').value = id;
      document.getElementById('btnText').innerText = 'Update Product';

      resetFileInputsRequired(false);
    });
}

function deleteProduct(id) {
  if (!confirm('Delete this product?')) return;

  fetch('productList?id=' + id, { method: 'DELETE' })
    .then(res => res.json())
    .then(data => {
      alert(data.message);
      if (data.success) loadProducts();
    });
}

function resetFileInputsRequired(required = true) {
  document.querySelector('[name="image1"]').required = required;
  document.querySelector('[name="image2"]').required = required;
}
