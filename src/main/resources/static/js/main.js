// Main JS file - The Midnight Café

// Cart State Management
const CART_STORAGE_KEY = 'midnight_cafe_cart';

function getCart() {
    try {
        const data = localStorage.getItem(CART_STORAGE_KEY);
        return data ? JSON.parse(data) : [];
    } catch (e) {
        return [];
    }
}

function saveCart(cart) {
    try {
        localStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cart));
    } catch (e) {
        console.error('Failed to save cart to localStorage', e);
    }
}

function getCartTotalCount() {
    const cart = getCart();
    return cart.reduce((total, item) => total + (parseInt(item.quantity, 10) || 1), 0);
}

function updateCartHeaderCount() {
    const totalCount = getCartTotalCount();
    const countElements = document.querySelectorAll('#headerCartCount, .cart-counter, .cart-count-badge');
    countElements.forEach(el => {
        el.textContent = totalCount;
        el.classList.remove('pop');
        // Trigger reflow to restart animation
        void el.offsetWidth;
        el.classList.add('pop');
    });
}

function showCartToast(message, icon = 'fa-check-circle') {
    const existing = document.querySelector('.cart-toast');
    if (existing) {
        existing.remove();
    }
    const toast = document.createElement('div');
    toast.className = 'cart-toast';
    toast.innerHTML = `<i class="fas ${icon}"></i> <span>${message}</span>`;
    document.body.appendChild(toast);
    setTimeout(() => {
        toast.classList.add('show');
    }, 50);
    setTimeout(() => {
        toast.classList.remove('show');
        setTimeout(() => toast.remove(), 400);
    }, 3200);
}

function addToCart(productId, name, price, quantity = 1, category = '') {
    const qty = Math.max(1, parseInt(quantity, 10) || 1);
    const cart = getCart();
    const existingIndex = cart.findIndex(item => (productId && item.id === productId) || item.name === name);
    if (existingIndex > -1) {
        cart[existingIndex].quantity = (parseInt(cart[existingIndex].quantity, 10) || 1) + qty;
    } else {
        cart.push({
            id: productId,
            name: name,
            price: price,
            category: category,
            quantity: qty
        });
    }
    saveCart(cart);
    updateCartHeaderCount();
    const qtyText = qty > 1 ? `${qty}× ` : '';
    showCartToast(`Added <strong>${qtyText}${name}</strong> to cart!`);
}

function addToCartNotification(productName, quantity = 1) {
    addToCart(null, productName, 0, quantity);
}

function incrementQty() {
    const input = document.getElementById('productQuantity');
    if (input) {
        let val = parseInt(input.value, 10) || 1;
        if (val < 99) {
            input.value = val + 1;
        }
    }
}

function decrementQty() {
    const input = document.getElementById('productQuantity');
    if (input) {
        let val = parseInt(input.value, 10) || 1;
        if (val > 1) {
            input.value = val - 1;
        }
    }
}

function handleAddToCart(name, productId, price, category = '') {
    const input = document.getElementById('productQuantity');
    const quantity = input ? (parseInt(input.value, 10) || 1) : 1;
    addToCart(productId, name, price, quantity, category);
}

document.addEventListener('DOMContentLoaded', () => {
    console.log('Midnight Coder initialized');

    // Initialize Cart Count on Page Load
    updateCartHeaderCount();

    // Mobile menu toggle logic
    const mobileMenuToggle = document.querySelector('.mobile-menu-toggle');
    const navMenu = document.querySelector('.nav-menu');

    if (mobileMenuToggle && navMenu) {
        mobileMenuToggle.addEventListener('click', () => {
            navMenu.classList.toggle('active');
            const icon = mobileMenuToggle.querySelector('i');
            if (navMenu.classList.contains('active')) {
                icon.classList.remove('fa-bars');
                icon.classList.add('fa-times');
                document.body.style.overflow = 'hidden';
            } else {
                icon.classList.remove('fa-times');
                icon.classList.add('fa-bars');
                document.body.style.overflow = '';
            }
        });

        // Close menu when clicking outside
        document.addEventListener('click', (e) => {
            if (!navMenu.contains(e.target) && !mobileMenuToggle.contains(e.target)) {
                navMenu.classList.remove('active');
                const icon = mobileMenuToggle.querySelector('i');
                icon.classList.remove('fa-times');
                icon.classList.add('fa-bars');
                document.body.style.overflow = '';
            }
        });
    }
});

function closePopup() {
    const popup = document.getElementById('welcomePopup');
    if (popup) {
        popup.style.opacity = '0';
        popup.style.transition = 'opacity 0.3s ease';
        setTimeout(() => {
            popup.remove();
            // Optional: clean up URL
            const url = new URL(window.location);
            url.searchParams.delete('loginSuccess');
            window.history.replaceState({}, document.title, url);
        }, 300);
        location.replace('/profile')
    }
}
