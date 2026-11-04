(() => {
  "use strict";

  const API_BASE_URL = window.location.origin;
  const PAGE_SIZE = 10;
  const CART_STORAGE_KEY = "shopsphere.cart.v1";
  const TOKEN_STORAGE_KEY = "shopsphere.accessToken";
  const EMAIL_STORAGE_KEY = "shopsphere.email";
  const imageFallback = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Crect width='100' height='100' rx='14' fill='%23f1f3ed'/%3E%3Cpath d='M28 37h44l-4 38H32l-4-38Zm12 2V31a10 10 0 0 1 20 0v8' fill='none' stroke='%2395a18e' stroke-width='3' stroke-linecap='round' stroke-linejoin='round'/%3E%3C/svg%3E";
  const state = {
    products: [],
    categories: [],
    page: 0,
    totalPages: 0,
    totalProducts: 0,
    cart: loadCart(),
    authMode: "login",
    toastTimer: null
  };
  const $ = (selector, root = document) => root.querySelector(selector);
  const $$ = (selector, root = document) => [...root.querySelectorAll(selector)];
  const currency = new Intl.NumberFormat(undefined, { style: "currency", currency: "USD" });

  function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, (character) => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;"
    })[character]);
  }

  function imageUrl(product) {
    const raw = product?.images?.find((image) => image?.imageUrl)?.imageUrl;
    if (!raw) return imageFallback;
    try {
      const url = /^https?:\/\//i.test(raw)
        ? new URL(raw)
        : new URL(raw.startsWith("/") ? raw : `/product_images/${encodeURIComponent(raw)}`, API_BASE_URL);
      return ["http:", "https:"].includes(url.protocol) ? url.href : imageFallback;
    } catch {
      return imageFallback;
    }
  }

  function loadCart() {
    try {
      const saved = JSON.parse(localStorage.getItem(CART_STORAGE_KEY) || "[]");
      return Array.isArray(saved) ? saved.filter((item) =>
        Number.isSafeInteger(item?.product?.id) && Number.isSafeInteger(item?.quantity) && item.quantity > 0
      ) : [];
    } catch (error) {
      console.error("Could not read the saved cart.", error);
      try { localStorage.removeItem(CART_STORAGE_KEY); } catch (storageError) {
        console.error("Could not clear the invalid saved cart.", storageError);
      }
      return [];
    }
  }

  function saveCart() {
    try {
      localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(state.cart));
    } catch (error) {
      console.error("Could not save the cart in this browser.", error);
      showToast("Your cart could not be saved in this browser.", true);
    }
    renderCart();
  }

  async function apiRequest(path, options = {}) {
    const response = await fetch(new URL(path, API_BASE_URL), {
      ...options,
      headers: {
        ...(options.body ? { "Content-Type": "application/json" } : {}),
        ...(options.headers || {})
      }
    });
    const body = await response.json().catch(() => null);
    if (!response.ok) {
      const message = body?.message || body?.error || `The request failed (${response.status}).`;
      const fields = body?.fieldErrors ? Object.values(body.fieldErrors).join(" ") : "";
      throw new Error([message, fields].filter(Boolean).join(" "));
    }
    return body;
  }

  function showToast(message, isError = false) {
    const toast = $("#toast");
    toast.textContent = message;
    toast.classList.toggle("error", isError);
    toast.classList.add("visible");
    window.clearTimeout(state.toastTimer);
    state.toastTimer = window.setTimeout(() => toast.classList.remove("visible"), 3000);
  }

  function stars(rating) {
    const safeRating = Math.max(0, Math.min(5, Math.round(Number(rating) || 0)));
    return `${"★".repeat(safeRating)}${"☆".repeat(5 - safeRating)}`;
  }

  function productCard(product) {
    const available = Number(product.stock) > 0;
    const image = escapeHtml(imageUrl(product));
    const name = escapeHtml(product.name || "Untitled product");
    const category = escapeHtml(product.category || "Uncategorized");
    const seller = escapeHtml(product.seller || "Shop seller");
    const rating = Number(product.rating) || 0;
    const reviews = Number(product.numOfReviews) || 0;
    return `<article class="product-card">
      <div class="product-image-wrap" data-action="details" data-id="${Number(product.id)}" tabindex="0" role="button" aria-label="View ${name} details">
        <img class="product-image" src="${image}" alt="${name}" loading="lazy" onerror="this.onerror=null;this.src='${imageFallback}'">
        <span class="product-badge">${category}</span>
      </div>
      <div class="product-card-body">
        <span class="product-category">${category}</span>
        <button class="product-name text-button" data-action="details" data-id="${Number(product.id)}" type="button">${name}</button>
        <div class="product-rating" aria-label="${rating.toFixed(1)} out of 5 stars">${stars(rating)} <span>${rating.toFixed(1)} · ${reviews} reviews</span></div>
        <div class="product-meta">Sold by ${seller}</div>
        <div class="product-price-row"><span class="product-price">${currency.format(Number(product.price) || 0)}</span>
          <span class="stock-status${available ? "" : " out"}">${available ? `${Number(product.stock)} in stock` : "Out of stock"}</span>
        </div>
        <div class="product-actions">
          <button class="button button-dark" data-action="add" data-id="${Number(product.id)}" type="button"${available ? "" : " disabled"}>Add to cart</button>
          <button class="button button-outline" data-action="details" data-id="${Number(product.id)}" type="button">Details</button>
        </div>
      </div>
    </article>`;
  }

  function renderProducts(products, container) {
    container.innerHTML = products.length ? products.map(productCard).join("") : "";
  }

  function renderCategories() {
    const grid = $("#category-grid");
    if (!state.categories.length) {
      grid.innerHTML = '<div class="category-loading">Categories will appear when products are available.</div>';
      return;
    }
    grid.innerHTML = state.categories.slice(0, 8).map((category, index) => `
      <button class="category-tile" type="button" data-category="${escapeHtml(category)}">
        <span class="category-ornament" aria-hidden="true">${["✳", "◌", "✧", "⌁"][index % 4]}</span>
        <span class="category-label">${escapeHtml(category)}</span><span class="category-arrow" aria-hidden="true">↗</span>
      </button>`).join("");
    const select = $("#category-filter");
    const selected = select.value;
    select.innerHTML = '<option value="">All categories</option>' +
      state.categories.map((category) => `<option value="${escapeHtml(category)}">${escapeHtml(category)}</option>`).join("");
    if (state.categories.includes(selected)) select.value = selected;
  }

  function updateCategories(products) {
    for (const product of products) {
      if (product.category && !state.categories.includes(product.category)) {
        state.categories.push(product.category);
      }
    }
    state.categories.sort((a, b) => a.localeCompare(b));
    renderCategories();
  }

  function activeFilters() {
    const values = {
      category: $("#category-filter").value,
      minPrice: $("#min-price").value,
      maxPrice: $("#max-price").value,
      keyword: $("#keyword").value.trim(),
      minRating: $("#min-rating").value,
      maxRating: $("#max-rating").value
    };
    return Object.fromEntries(Object.entries(values).filter(([, value]) => value !== ""));
  }

  async function loadProducts(page = 0) {
    state.page = page;
    const list = $("#product-list");
    $("#product-error").hidden = true;
    $("#empty-state").hidden = true;
    $("#pagination").innerHTML = "";
    list.innerHTML = '<div class="loading-state"><span class="spinner"></span> Loading products...</div>';
    const filters = activeFilters();
    const params = new URLSearchParams({ page: String(page), size: String(PAGE_SIZE) });
    for (const [key, value] of Object.entries(filters)) params.set(key, value);
    const endpoint = Object.keys(filters).length ? "/api/products/search" : "/api/products";
    try {
      const result = await apiRequest(`${endpoint}?${params}`);
      state.products = Array.isArray(result?.products) ? result.products : [];
      state.totalPages = Number(result?.totalPages) || 0;
      state.totalProducts = Number(result?.totalProducts) || 0;
      updateCategories(state.products);
      renderProducts(state.products, list);
      renderProducts(state.products.slice(0, 4), $("#featured-products"));
      $("#results-count").textContent = `${state.totalProducts} ${state.totalProducts === 1 ? "product" : "products"}`;
      $("#empty-state").hidden = state.products.length !== 0;
      renderPagination();
    } catch (error) {
      console.error("Product catalog request failed.", error);
      list.innerHTML = "";
      $("#featured-products").innerHTML = "";
      $("#product-error").textContent = `Products could not be loaded. ${error.message}`;
      $("#product-error").hidden = false;
      $("#results-count").textContent = "";
    }
  }

  function renderPagination() {
    const pagination = $("#pagination");
    if (state.totalPages <= 1) return;
    const buttons = [];
    const addButton = (page, label, active = false, disabled = false) => {
      buttons.push(`<button type="button" class="page-button${active ? " current" : ""}" data-page="${page}"${active ? ' aria-current="page"' : ""}${disabled ? " disabled" : ""}>${label}</button>`);
    };
    addButton(Math.max(0, state.page - 1), "←", false, state.page === 0);
    const first = Math.max(0, Math.min(state.page - 2, state.totalPages - 5));
    const last = Math.min(state.totalPages, Math.max(5, state.page + 3));
    if (first > 0) buttons.push('<span class="page-ellipsis">…</span>');
    for (let page = first; page < last; page += 1) addButton(page, String(page + 1), page === state.page);
    if (last < state.totalPages) buttons.push('<span class="page-ellipsis">…</span>');
    addButton(Math.min(state.totalPages - 1, state.page + 1), "→", false, state.page >= state.totalPages - 1);
    pagination.innerHTML = buttons.join("");
  }

  function addToCart(product) {
    if (!product || Number(product.stock) < 1) {
      showToast("This product is currently out of stock.", true);
      return;
    }
    const existing = state.cart.find((item) => item.product.id === product.id);
    if (existing) {
      if (existing.quantity >= Number(product.stock)) {
        showToast("You have reached the available stock for this product.", true);
        return;
      }
      existing.quantity += 1;
      existing.product = product;
    } else {
      state.cart.push({ product, quantity: 1 });
    }
    saveCart();
    showToast(`${product.name} added to your cart.`);
  }

  function renderCart() {
    const count = state.cart.reduce((total, item) => total + item.quantity, 0);
    $("#cart-count").textContent = String(count);
    $("#cart-heading-count").textContent = count ? `(${count})` : "";
    const target = $("#cart-items");
    if (!state.cart.length) {
      target.innerHTML = '<div class="cart-empty"><strong>Your cart is taking a little break.</strong>Find something lovely to bring home.</div>';
      $("#cart-summary").hidden = true;
      $("#checkout-panel").hidden = true;
      $(".cart-footnote").hidden = false;
      return;
    }
    target.innerHTML = state.cart.map(({ product, quantity }) => `
      <article class="cart-item">
        <img src="${escapeHtml(imageUrl(product))}" alt="" onerror="this.onerror=null;this.src='${imageFallback}'">
        <div><p class="cart-item-name">${escapeHtml(product.name || "Product")}</p>
          <span class="cart-item-price">${currency.format(Number(product.price) || 0)} each</span>
          <div class="quantity-control">
            <button type="button" data-cart-action="decrease" data-id="${Number(product.id)}" aria-label="Decrease quantity">−</button>
            <span>${quantity}</span>
            <button type="button" data-cart-action="increase" data-id="${Number(product.id)}" aria-label="Increase quantity"${quantity >= Number(product.stock) ? " disabled" : ""}>+</button>
            <button type="button" class="remove-item" data-cart-action="remove" data-id="${Number(product.id)}">Remove</button>
          </div>
        </div><span class="cart-line-total">${currency.format((Number(product.price) || 0) * quantity)}</span>
      </article>`).join("");
    const total = state.cart.reduce((sum, item) => sum + (Number(item.product.price) || 0) * item.quantity, 0);
    $("#cart-total").textContent = currency.format(total);
    $("#cart-summary").hidden = false;
    $(".cart-footnote").hidden = false;
    if (localStorage.getItem(TOKEN_STORAGE_KEY)) {
      $("#auth-form").hidden = true;
      $("#place-order").hidden = false;
      $("#checkout-heading").textContent = `Ready to order${localStorage.getItem(EMAIL_STORAGE_KEY) ? `, ${localStorage.getItem(EMAIL_STORAGE_KEY)}` : ""}`;
    }
  }

  function changeCart(productId, action) {
    const item = state.cart.find((entry) => entry.product.id === productId);
    if (!item) return;
    if (action === "remove" || (action === "decrease" && item.quantity === 1)) {
      state.cart = state.cart.filter((entry) => entry !== item);
    } else if (action === "decrease") {
      item.quantity -= 1;
    } else if (action === "increase") {
      if (item.quantity >= Number(item.product.stock)) {
        showToast("You have reached the available stock for this product.", true);
        return;
      }
      item.quantity += 1;
    }
    saveCart();
  }

  async function showProductDetails(productId) {
    const dialog = $("#detail-dialog");
    const content = $("#detail-content");
    content.innerHTML = '<div class="loading-state"><span class="spinner"></span> Loading product details...</div>';
    dialog.showModal();
    try {
      const product = await apiRequest(`/api/products/${encodeURIComponent(productId)}`);
      const image = escapeHtml(imageUrl(product));
      const rating = Number(product.rating) || 0;
      content.innerHTML = `<div class="detail-layout">
        <div class="detail-image-pane"><img src="${image}" alt="${escapeHtml(product.name)}" onerror="this.onerror=null;this.src='${imageFallback}'"></div>
        <div class="detail-copy">
          <span class="eyebrow">${escapeHtml(product.category || "ShopSphere find")}</span>
          <h2>${escapeHtml(product.name || "Product details")}</h2>
          <div class="product-rating">${stars(rating)} <span>${rating.toFixed(1)} · ${Number(product.numOfReviews) || 0} reviews</span></div>
          <div class="detail-price">${currency.format(Number(product.price) || 0)}</div>
          <p class="detail-description">${escapeHtml(product.description || "No description is available for this product yet.")}</p>
          <div class="detail-facts">
            <div class="detail-fact"><span>Seller</span><strong>${escapeHtml(product.seller || "Shop seller")}</strong></div>
            <div class="detail-fact"><span>Availability</span><strong>${Number(product.stock) > 0 ? `${Number(product.stock)} in stock` : "Out of stock"}</strong></div>
          </div>
          <button class="button button-dark" data-action="detail-add" data-id="${Number(product.id)}"${Number(product.stock) > 0 ? "" : " disabled"}>Add to cart <span aria-hidden="true">→</span></button>
          <section class="reviews"><h3>Customer reviews</h3><div class="reviews-content"><span class="reviews-empty">Loading reviews...</span></div></section>
        </div>
      </div>`;
      loadReviews(product.id);
    } catch (error) {
      console.error("Product detail request failed.", error);
      content.innerHTML = `<div class="error-state">Product details could not be loaded. ${escapeHtml(error.message)}</div>`;
    }
  }

  async function loadReviews(productId) {
    const target = $(".reviews-content", $("#detail-content"));
    if (!target) return;
    try {
      const reviews = await apiRequest(`/api/products/${encodeURIComponent(productId)}/reviews`);
      if (!Array.isArray(reviews) || !reviews.length) {
        target.innerHTML = '<p class="reviews-empty">No customer reviews yet.</p>';
        return;
      }
      target.innerHTML = reviews.map((review) => `
        <article class="review-item">
          <span class="review-stars" aria-label="${Number(review.rating) || 0} out of 5 stars">${stars(review.rating)}</span>
          <p>${escapeHtml(review.comment || "No written comment.")}</p>
        </article>`).join("");
    } catch (error) {
      console.error("Product reviews request failed.", error);
      target.innerHTML = `<p class="reviews-empty">Reviews could not be loaded: ${escapeHtml(error.message)}</p>`;
    }
  }

  function openCheckout() {
    $("#checkout-panel").hidden = false;
    $("#cart-summary").hidden = true;
    $("#checkout-status").textContent = "";
    $("#checkout-status").classList.remove("success");
    if (localStorage.getItem(TOKEN_STORAGE_KEY)) {
      $("#auth-form").hidden = true;
      $("#place-order").hidden = false;
      $("#checkout-heading").textContent = `Ready to order${localStorage.getItem(EMAIL_STORAGE_KEY) ? `, ${localStorage.getItem(EMAIL_STORAGE_KEY)}` : ""}`;
    } else {
      $("#auth-form").hidden = false;
      $("#place-order").hidden = true;
      $("#checkout-heading").textContent = state.authMode === "login" ? "Sign in to place your order" : "Create an account to order";
    }
  }

  async function submitAuth(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const submit = $("#auth-submit");
    const status = $("#checkout-status");
    status.textContent = "";
    submit.disabled = true;
    submit.textContent = state.authMode === "login" ? "Signing in..." : "Creating account...";
    try {
      const values = Object.fromEntries(new FormData(form));
      const response = await apiRequest(`/api/auth/${state.authMode}`, {
        method: "POST",
        body: JSON.stringify(values)
      });
      if (!response?.accessToken) throw new Error("The server did not return an access token.");
      localStorage.setItem(TOKEN_STORAGE_KEY, response.accessToken);
      localStorage.setItem(EMAIL_STORAGE_KEY, values.email);
      $("#checkout-heading").textContent = `Ready to order, ${values.email}`;
      form.hidden = true;
      $("#place-order").hidden = false;
      showToast("You are signed in. Your cart is ready.");
    } catch (error) {
      console.error("Account request failed.", error);
      status.textContent = error.message;
    } finally {
      submit.disabled = false;
      submit.textContent = state.authMode === "login" ? "Sign in" : "Create account";
    }
  }

  async function placeOrder() {
    const button = $("#place-order");
    const status = $("#checkout-status");
    const token = localStorage.getItem(TOKEN_STORAGE_KEY);
    if (!token) {
      status.textContent = "Sign in before placing your order.";
      $("#auth-form").hidden = false;
      button.hidden = true;
      return;
    }
    button.disabled = true;
    button.textContent = "Placing your order...";
    status.textContent = "";
    try {
      const order = await apiRequest("/api/orders", {
        method: "POST",
        headers: { Authorization: `Bearer ${token}` },
        body: JSON.stringify({
          items: state.cart.map(({ product, quantity }) => ({
            productId: Number(product.id),
            quantity
          }))
        })
      });
      state.cart = [];
      saveCart();
      $("#checkout-panel").hidden = false;
      $("#checkout-panel").querySelector(".checkout-title").innerHTML =
        `<div><span class="eyebrow">Order confirmed</span><h3>Thank you for your order!</h3></div>`;
      $("#checkout-status").textContent = `Order #${order.orderId} is ${String(order.status || "placed").toLowerCase()}. Total: ${currency.format(Number(order.totalAmount) || 0)}.`;
      $("#checkout-status").classList.add("success");
      $(".cart-footnote").hidden = true;
      showToast(`Order #${order.orderId} placed successfully.`);
    } catch (error) {
      console.error("Order submission failed.", error);
      status.textContent = `${error.message} Your cart has been kept so you can try again.`;
    } finally {
      button.disabled = false;
      button.innerHTML = 'Place order <span aria-hidden="true">→</span>';
    }
  }

  function resetFilters() {
    $("#filter-form").reset();
    $("#keyword").value = "";
    loadProducts(0);
  }

  function initialize() {
    $("#current-year").textContent = String(new Date().getFullYear());
    renderCart();
    loadProducts(0);

    $("#search-form").addEventListener("submit", (event) => {
      event.preventDefault();
      loadProducts(0);
      $("#products").scrollIntoView({ behavior: "smooth" });
    });
    $("#filter-form").addEventListener("submit", (event) => {
      event.preventDefault();
      loadProducts(0);
    });
    $("#clear-filters").addEventListener("click", resetFilters);
    $("#empty-reset").addEventListener("click", resetFilters);
    $("#category-grid").addEventListener("click", (event) => {
      const tile = event.target.closest("[data-category]");
      if (!tile) return;
      $("#category-filter").value = tile.dataset.category;
      loadProducts(0);
      $("#products").scrollIntoView({ behavior: "smooth" });
    });
    $("#pagination").addEventListener("click", (event) => {
      const button = event.target.closest("[data-page]");
      if (button && !button.disabled) {
        loadProducts(Number(button.dataset.page));
        $("#products").scrollIntoView({ behavior: "smooth" });
      }
    });
    for (const container of [$("#product-list"), $("#featured-products")]) {
      container.addEventListener("click", (event) => {
        const button = event.target.closest("[data-action]");
        if (!button) return;
        const product = state.products.find((item) => Number(item.id) === Number(button.dataset.id));
        if (button.dataset.action === "add" && product) addToCart(product);
        if (button.dataset.action === "details") showProductDetails(button.dataset.id);
      });
      container.addEventListener("keydown", (event) => {
        const target = event.target.closest('[data-action="details"][role="button"]');
        if (target && (event.key === "Enter" || event.key === " ")) {
          event.preventDefault();
          showProductDetails(target.dataset.id);
        }
      });
    }
    $("#detail-content").addEventListener("click", (event) => {
      const button = event.target.closest('[data-action="detail-add"]');
      if (!button) return;
      const product = state.products.find((item) => Number(item.id) === Number(button.dataset.id));
      if (product) addToCart(product);
    });
    $("#cart-items").addEventListener("click", (event) => {
      const button = event.target.closest("[data-cart-action]");
      if (button) changeCart(Number(button.dataset.id), button.dataset.cartAction);
    });
    $("#open-cart").addEventListener("click", () => $("#cart-dialog").showModal());
    $("#checkout-button").addEventListener("click", openCheckout);
    $("#back-to-cart").addEventListener("click", () => {
      $("#checkout-panel").hidden = true;
      $("#cart-summary").hidden = false;
    });
    $$(".auth-tab").forEach((tab) => tab.addEventListener("click", () => {
      state.authMode = tab.dataset.authMode;
      $$(".auth-tab").forEach((button) => button.classList.toggle("active", button === tab));
      $("#auth-submit").textContent = state.authMode === "login" ? "Sign in" : "Create account";
      $("#checkout-heading").textContent = state.authMode === "login" ? "Sign in to place your order" : "Create an account to order";
      $("#auth-form input[name='password']").autocomplete = state.authMode === "login" ? "current-password" : "new-password";
      $("#checkout-status").textContent = "";
    }));
    $("#auth-form").addEventListener("submit", submitAuth);
    $("#place-order").addEventListener("click", placeOrder);
    $$("[data-close-dialog]").forEach((button) => button.addEventListener("click", () => button.closest("dialog").close()));
    [$("#detail-dialog"), $("#cart-dialog")].forEach((dialog) => {
      dialog.addEventListener("click", (event) => {
        if (event.target === dialog) dialog.close();
      });
    });
  }

  initialize();
})();
