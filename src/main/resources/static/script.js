const API_URL = 'http://localhost:8080/api';

let auctions = [];
let currentUser = null;
let selectedCategory = null;

function toggleTheme() {
    const isDark = document.body.classList.toggle('dark-theme');
    localStorage.setItem('theme', isDark ? 'dark' : 'light');
    updateThemeIcons();
}
function loadTheme() {
    if (localStorage.getItem('theme') === 'dark') document.body.classList.add('dark-theme');
    updateThemeIcons();
}
function updateThemeIcons() {
    const isDark = document.body.classList.contains('dark-theme');
    ['buyerThemeIcon', 'sellerThemeIcon', 'homeThemeIcon'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.className = isDark ? 'fas fa-sun' : 'fas fa-moon';
    });
}

function filterByCategory(category) {
    selectedCategory = category;
    if (currentUser && currentUser.userType === 'buyer') renderBuyerAuctions();
}
function showAllAuctions() {
    selectedCategory = null;
    if (currentUser && currentUser.userType === 'buyer') renderBuyerAuctions();
}

function checkAuth() {
    const user = localStorage.getItem('currentUser');
    if (user) { currentUser = JSON.parse(user); updateUIForLoggedInUser(); }
}

function updateUIForLoggedInUser() {
    document.getElementById('homePage').style.display = 'none';
    if (currentUser.userType === 'buyer') {
        document.getElementById('buyerDashboard').style.display = 'block';
        document.getElementById('sellerDashboard').style.display = 'none';
        document.getElementById('buyerName').textContent = currentUser.name;
        updateBuyerWallet();
        renderBuyerAuctions();
        renderBuyerOrders();
    } else {
        document.getElementById('sellerDashboard').style.display = 'block';
        document.getElementById('buyerDashboard').style.display = 'none';
        document.getElementById('sellerName').textContent = currentUser.name;
        updateSellerWallet();
        renderSellerAuctions();
        updateSellerStats();
        renderOrders();
    }
}

function updateUIForLoggedOutUser() {
    document.getElementById('homePage').style.display = 'block';
    document.getElementById('buyerDashboard').style.display = 'none';
    document.getElementById('sellerDashboard').style.display = 'none';
}

function logout() {
    localStorage.removeItem('currentUser');
    currentUser = null;
    updateUIForLoggedOutUser();
    showNotification('Logged out successfully!');
}


async function updateBuyerWallet() {
    const res = await fetch(`${API_URL}/users/wallet/${encodeURIComponent(currentUser.email)}`);
    const bal = await res.json();
    document.getElementById('buyerWalletBalance').textContent = bal.toFixed(2);
}

async function updateSellerWallet() {
    const res = await fetch(`${API_URL}/users/wallet/${encodeURIComponent(currentUser.email)}`);
    const bal = await res.json();
    document.getElementById('sellerWalletBalance').textContent = bal.toFixed(2);
}

function addFundsToBuyer() {
    new bootstrap.Modal(document.getElementById('addFundsModal')).show();
}

function formatTime(seconds) {
    const h = Math.floor(seconds / 3600), m = Math.floor((seconds % 3600) / 60);
    if (h > 0) return `${h}h ${m}m`;
    if (m > 0) return `${m}m`;
    return `${seconds}s`;
}
function createAuctionCard(auction) {
    const isWinner = currentUser && auction.winnerId === currentUser.email;
    const isStopped = !auction.isActive;

    let actionBtn = '';
    if (auction.isActive) {
        actionBtn = `
            <div class="input-group mb-2">
                <span class="input-group-text">₹</span>
                <input type="number" class="form-control" id="bidInput_${auction.id}"
                    placeholder="Min: ${auction.currentBid + 1}" min="${auction.currentBid + 1}">
            </div>
            <button class="bid-btn" onclick="placeBid(${auction.id})">
                <i class="fas fa-gavel"></i> Place Bid
            </button>`;
    } else if (isWinner) {
        actionBtn = `
            <div class="alert alert-success py-1 px-2 mb-2 small"><i class="fas fa-trophy"></i> You won! Winning bid: ₹${auction.winnerBid}</div>
            <button class="btn btn-success w-100" onclick="addToCart(${auction.id})">
                <i class="fas fa-cart-plus"></i> Add to Cart
            </button>`;
    } else if (isStopped) {
        actionBtn = `<div class="alert alert-secondary py-1 px-2 mb-0 small text-center">Auction ended</div>`;
    }

    return `
        <div class="col-md-6 col-lg-4">
            <div class="auction-card" data-id="${auction.id}">
                <img src="${auction.image}" alt="${auction.title}">
                <div class="auction-card-body">
                    <span class="badge bg-info mb-2">${auction.category}</span>
                    <h5>${auction.title}</h5>
                    <p class="text-muted small">${auction.description ? auction.description.substring(0, 60) + '...' : ''}</p>
                    <div class="timer">
                        <i class="fas fa-clock"></i>
                        <span class="time-remaining" data-time="${auction.timeLeft}">${auction.isActive ? formatTime(auction.timeLeft) : 'Ended'}</span>
                    </div>
                    <div class="current-bid">
                        <div>
                            <div class="bid-label">Current Bid</div>
                            <div class="bid-amount">₹${auction.currentBid}</div>
                        </div>
                        <div class="text-end">
                            <div class="bid-label">${auction.bids} Bids</div>
                        </div>
                    </div>
                    ${actionBtn}
                </div>
            </div>
        </div>`;
}


async function renderBuyerAuctions() {
    const grid = document.getElementById('buyer-auction-grid');
    if (!grid) return;
    const url = selectedCategory ? `${API_URL}/auctions/category/${selectedCategory}` : `${API_URL}/auctions/all`;
    const res = await fetch(url);
    auctions = await res.json();
    if (auctions.length === 0) {
        grid.innerHTML = '<div class="col-12 text-center text-muted py-5"><i class="fas fa-gavel fa-3x mb-3"></i><p>No auctions available</p></div>';
    } else {
        grid.innerHTML = auctions.map(a => createAuctionCard(a)).join('');
    }
}


function createSellerAuctionCard(auction) {
    const statusBadge = auction.isActive
        ? '<span class="badge bg-success">Active</span>'
        : '<span class="badge bg-danger">Stopped</span>';
    const winnerInfo = !auction.isActive && auction.winnerName
        ? `<div class="alert alert-info py-1 px-2 mb-2 small"><i class="fas fa-trophy"></i> Winner: <strong>${auction.winnerName}</strong> — ₹${auction.winnerBid}</div>`
        : '';

    return `
        <div class="col-md-6 col-lg-4">
            <div class="auction-card" data-id="${auction.id}">
                <img src="${auction.image}" alt="${auction.title}">
                <div class="auction-card-body">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <h5 class="mb-0">${auction.title}</h5>
                        ${statusBadge}
                    </div>
                    <span class="badge bg-info mb-2">${auction.category}</span>
                    <p class="text-muted small">${auction.description ? auction.description.substring(0, 60) + '...' : ''}</p>
                    <div class="timer">
                        <i class="fas fa-clock"></i>
                        <span class="time-remaining" data-time="${auction.timeLeft}">${auction.isActive ? formatTime(auction.timeLeft) : 'Ended'}</span>
                    </div>
                    <div class="current-bid">
                        <div>
                            <div class="bid-label">Current Bid</div>
                            <div class="bid-amount">₹${auction.currentBid}</div>
                        </div>
                        <div class="text-end">
                            <div class="bid-label">${auction.bids} Bids</div>
                        </div>
                    </div>
                    ${winnerInfo}
                    <div class="d-flex gap-2 mb-2">
                        <button class="btn btn-sm btn-info flex-fill" onclick="viewBids(${auction.id})">
                            <i class="fas fa-eye"></i> View Bids
                        </button>
                        ${auction.isActive
                            ? `<button class="btn btn-sm btn-warning flex-fill" onclick="stopBid(${auction.id})">
                                <i class="fas fa-stop"></i> Stop
                               </button>`
                            : `<button class="btn btn-sm btn-success flex-fill" onclick="startBid(${auction.id})">
                                <i class="fas fa-play"></i> Start
                               </button>`
                        }
                    </div>
                    <button class="btn btn-sm btn-outline-danger w-100" onclick="deleteProduct(${auction.id})">
                        <i class="fas fa-trash"></i> Delete
                    </button>
                </div>
            </div>
        </div>`;
}

async function renderSellerAuctions() {
    const grid = document.getElementById('seller-auction-grid');
    if (!grid) return;
    const res = await fetch(`${API_URL}/auctions/seller/${encodeURIComponent(currentUser.email)}`);
    const products = await res.json();
    grid.innerHTML = products.length === 0
        ? '<div class="col-12 text-center text-muted py-5"><i class="fas fa-box-open fa-3x mb-3"></i><p>No products listed yet</p></div>'
        : products.map(a => createSellerAuctionCard(a)).join('');
}

async function updateSellerStats() {
    const r1 = await fetch(`${API_URL}/auctions/seller/${encodeURIComponent(currentUser.email)}`);
    const products = await r1.json();
    const r2 = await fetch(`${API_URL}/orders/pending/${encodeURIComponent(currentUser.email)}`);
    const pending = await r2.json();
    const r3 = await fetch(`${API_URL}/orders/accepted/${encodeURIComponent(currentUser.email)}`);
    const accepted = await r3.json();
    document.getElementById('activeListingsCount').textContent = products.filter(a => a.isActive).length;
    document.getElementById('pendingOrdersCount').textContent = pending.length;
    document.getElementById('acceptedOrdersCount').textContent = accepted.length;
}


async function viewBids(productId) {
    const r1 = await fetch(`${API_URL}/auctions/${productId}`);
    const product = await r1.json();
    const r2 = await fetch(`${API_URL}/bids/auction/${productId}`);
    const bidders = await r2.json();
    document.getElementById('bidProductName').textContent = product.title;
    const tbody = document.getElementById('bidsTableBody');
    tbody.innerHTML = bidders.length === 0
        ? '<tr><td colspan="3" class="text-center text-muted">No bids yet</td></tr>'
        : bidders.map(b => `<tr><td>${b.name}</td><td>₹${b.amount}</td><td>${b.time}</td></tr>`).join('');
    new bootstrap.Modal(document.getElementById('viewBidsModal')).show();
}


async function stopBid(productId) {
    const res = await fetch(`${API_URL}/auctions/${productId}/stop`, { method: 'PUT' });
    const auction = await res.json();
    if (auction.winnerName) {
        showNotification(`Bidding stopped! Winner: ${auction.winnerName} — ₹${auction.winnerBid}`);
    } else {
        showNotification('Bidding stopped! No bids were placed.');
    }
    updateSellerWallet();
    renderSellerAuctions();
    updateSellerStats();
    renderOrders();
}

async function startBid(productId) {
    const res = await fetch(`${API_URL}/auctions/${productId}`);
    const product = await res.json();
    product.isActive = true;
    product.winnerId = null;
    product.winnerName = null;
    product.winnerBid = null;
    await fetch(`${API_URL}/auctions/${productId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(product)
    });
    showNotification('Bidding resumed!');
    renderSellerAuctions();
    updateSellerStats();
}

async function deleteProduct(productId) {
    if (confirm('Delete this product?')) {
        await fetch(`${API_URL}/auctions/${productId}`, { method: 'DELETE' });
        showNotification('Product deleted!');
        renderSellerAuctions();
        updateSellerStats();
    }
}

async function renderOrders() {
    if (!currentUser || currentUser.userType !== 'seller') return;
    const r1 = await fetch(`${API_URL}/orders/pending/${encodeURIComponent(currentUser.email)}`);
    const pendingOrders = await r1.json();
    const r2 = await fetch(`${API_URL}/orders/accepted/${encodeURIComponent(currentUser.email)}`);
    const acceptedOrders = await r2.json();

    const pendingTable = document.getElementById('pendingOrdersTable');
    pendingTable.innerHTML = pendingOrders.length === 0
        ? '<tr><td colspan="6" class="text-center text-muted">No pending orders</td></tr>'
        : pendingOrders.map(o => `
            <tr>
                <td>#${o.id}</td>
                <td>${o.productName}</td>
                <td>${o.buyer}</td>
                <td>₹${o.amount}</td>
                <td>${o.date}</td>
                <td>
                    <button class="btn btn-sm btn-success" onclick="acceptOrder(${o.id})"><i class="fas fa-check"></i> Accept</button>
                    <button class="btn btn-sm btn-danger" onclick="rejectOrder(${o.id})"><i class="fas fa-times"></i> Reject</button>
                </td>
            </tr>`).join('');

    const acceptedTable = document.getElementById('acceptedOrdersTable');
    acceptedTable.innerHTML = acceptedOrders.length === 0
        ? '<tr><td colspan="6" class="text-center text-muted">No accepted orders</td></tr>'
        : acceptedOrders.map(o => `
            <tr>
                <td>#${o.id}</td>
                <td>${o.productName}</td>
                <td>${o.buyer}</td>
                <td>₹${o.amount}</td>
                <td>${o.date}</td>
                <td><span class="badge bg-success">${o.status}</span></td>
            </tr>`).join('');

    updateSellerStats();
}

async function acceptOrder(orderId) {
    await fetch(`${API_URL}/orders/${orderId}/accept`, { method: 'PUT' });
    showNotification('Order accepted!');
    renderOrders();
}
async function rejectOrder(orderId) {
    if (confirm('Reject this order?')) {
        await fetch(`${API_URL}/orders/${orderId}`, { method: 'DELETE' });
        showNotification('Order rejected!');
        renderOrders();
    }
}

async function renderBuyerOrders() {
    const tbody = document.getElementById('buyerOrdersTable');
    if (!tbody) return;
    const res = await fetch(`${API_URL}/orders/buyer/${encodeURIComponent(currentUser.email)}`);
    const orders = await res.json();
    tbody.innerHTML = orders.length === 0
        ? '<tr><td colspan="5" class="text-center text-muted">No orders yet</td></tr>'
        : orders.map(o => `
            <tr>
                <td>#${o.id}</td>
                <td>${o.productName}</td>
                <td>₹${o.amount}</td>
                <td>${o.date}</td>
                <td><span class="badge ${o.status === 'accepted' ? 'bg-success' : 'bg-warning text-dark'}">${o.status}</span></td>
            </tr>`).join('');
}

async function placeBid(auctionId) {
    if (!currentUser) {
        showNotification('Please login to bid!', 'warning');
        new bootstrap.Modal(document.getElementById('loginModal')).show();
        return;
    }
    const res = await fetch(`${API_URL}/auctions/${auctionId}`);
    const auction = await res.json();
    if (!auction || !auction.isActive) { showNotification('This auction is not active!', 'warning'); return; }

    const input = document.getElementById(`bidInput_${auctionId}`);
    const bidAmount = parseInt(input ? input.value : 0);
    const minBid = auction.currentBid + 1;

    if (!bidAmount || bidAmount < minBid) {
        showNotification(`Bid must be at least ₹${minBid}`, 'danger');
        return;
    }

    auction.currentBid = bidAmount;
    auction.bids = (auction.bids || 0) + 1;

    await fetch(`${API_URL}/auctions/${auctionId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(auction)
    });

    await fetch(`${API_URL}/bids`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            auctionId: auctionId,
            name: currentUser.name,
            bidderEmail: currentUser.email,
            amount: bidAmount,
            time: new Date().toLocaleTimeString()
        })
    });

    showNotification(`Bid of ₹${bidAmount} placed!`);
    renderBuyerAuctions();
}

async function addToCart(auctionId) {
    const res = await fetch(`${API_URL}/auctions/${auctionId}`);
    const auction = await res.json();
    if (!auction || auction.winnerId !== currentUser.email) {
        showNotification('Only the winner can add this to cart!', 'warning');
        return;
    }
    await fetch(`${API_URL}/cart/${encodeURIComponent(currentUser.email)}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            productId: auctionId,
            productName: auction.title,
            amount: auction.winnerBid,
            image: auction.image,
            sellerId: auction.sellerId
        })
    });
    showNotification('Added to cart! Go to My Cart to place your order.');
    renderBuyerCart();
}


async function placeOrderFromCart(productId, productName, amount, sellerId) {
 
    const walletRes = await fetch(`${API_URL}/users/wallet/${encodeURIComponent(currentUser.email)}`);
    const balance = await walletRes.json();
    if (balance < amount) {
        showNotification(`Insufficient wallet balance! You need ₹${amount} but have ₹${balance.toFixed(2)}`, 'danger');
        return;
    }

    await fetch(`${API_URL}/users/wallet/debit`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: currentUser.email, amount })
    });

    await fetch(`${API_URL}/users/wallet/credit`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: sellerId, amount })
    });

    await fetch(`${API_URL}/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            productId,
            productName,
            buyer: currentUser.name,
            buyerEmail: currentUser.email,
            amount,
            date: new Date().toISOString().split('T')[0],
            sellerId
        })
    });


    await fetch(`${API_URL}/cart/${encodeURIComponent(currentUser.email)}/${productId}`, { method: 'DELETE' });

    showNotification(`Order placed! ₹${amount} debited from your wallet.`);
    updateBuyerWallet();
    renderBuyerCart();
    renderBuyerOrders();
    
    new bootstrap.Tab(document.getElementById('buyerOrdersTab')).show();
}

async function renderBuyerCart() {
    const tbody = document.getElementById('buyerCartTable');
    if (!tbody) return;
    const res = await fetch(`${API_URL}/cart/${encodeURIComponent(currentUser.email)}`);
    const items = await res.json();
    tbody.innerHTML = items.length === 0
        ? '<tr><td colspan="5" class="text-center text-muted">Cart is empty</td></tr>'
        : items.map(i => `
            <tr>
                <td><img src="${i.image}" style="width:40px;height:40px;object-fit:cover;border-radius:4px"> ${i.productName}</td>
                <td>₹${i.amount}</td>
                <td><span class="badge bg-success">Won</span></td>
                <td>
                    <button class="btn btn-sm btn-primary" onclick="placeOrderFromCart(${i.productId}, '${i.productName.replace(/'/g, "\\'")  }', ${i.amount}, '${i.sellerId}')">
                        <i class="fas fa-shopping-bag"></i> Place Order
                    </button>
                </td>
                <td><button class="btn btn-sm btn-outline-danger" onclick="removeFromCart(${i.productId})"><i class="fas fa-trash"></i></button></td>
            </tr>`).join('');
}

async function removeFromCart(productId) {
    await fetch(`${API_URL}/cart/${encodeURIComponent(currentUser.email)}/${productId}`, { method: 'DELETE' });
    renderBuyerCart();
}


function updateTimers() {
    document.querySelectorAll('.time-remaining').forEach(timer => {
        let timeLeft = parseInt(timer.getAttribute('data-time'));
        if (isNaN(timeLeft) || timeLeft <= 0) return;
        timeLeft--;
        timer.setAttribute('data-time', timeLeft);
        timer.textContent = formatTime(timeLeft);
    });
}


function showNotification(message, type = 'success') {
    const n = document.createElement('div');
    n.className = `alert alert-${type} position-fixed top-0 start-50 translate-middle-x mt-3`;
    n.style.zIndex = '9999';
    n.innerHTML = `<i class="fas fa-${type === 'success' ? 'check-circle' : 'exclamation-circle'}"></i> ${message}`;
    document.body.appendChild(n);
    setTimeout(() => n.remove(), 3000);
}


document.addEventListener('DOMContentLoaded', () => {
   
    document.getElementById('registerForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        const user = {
            userType: document.getElementById('registerUserType').value,
            name: document.getElementById('registerName').value,
            email: document.getElementById('registerEmail').value,
            password: document.getElementById('registerPassword').value
        };
        if (user.password !== document.getElementById('registerConfirmPassword').value) {
            showNotification('Passwords do not match!', 'danger'); return;
        }
        const res = await fetch(`${API_URL}/users/register`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(user)
        });
        if (res.ok) {
            showNotification('Registration successful!');
            bootstrap.Modal.getInstance(document.getElementById('registerModal')).hide();
            new bootstrap.Modal(document.getElementById('loginModal')).show();
            this.reset();
        } else {
            const err = await res.json();
            showNotification(err.error || 'Registration failed!', 'danger');
        }
    });

    document.getElementById('loginForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        const res = await fetch(`${API_URL}/users/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: document.getElementById('loginEmail').value,
                password: document.getElementById('loginPassword').value
            })
        });
        if (res.ok) {
            const user = await res.json();
            currentUser = { userType: user.userType, name: user.name, email: user.email };
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            showNotification('Login successful!');
            updateUIForLoggedInUser();
            bootstrap.Modal.getInstance(document.getElementById('loginModal')).hide();
            this.reset();
        } else {
            showNotification('Invalid credentials!', 'danger');
        }
    });

    document.getElementById('addProductForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        const imageFile = document.getElementById('productImageFile').files[0];
        if (!imageFile) { showNotification('Please select an image!', 'danger'); return; }
        const reader = new FileReader();
        reader.onload = async function(event) {
            const newProduct = {
                title: document.getElementById('productName').value,
                category: document.getElementById('productCategory').value,
                description: document.getElementById('productDescription').value,
                image: event.target.result,
                currentBid: parseInt(document.getElementById('productStartBid').value),
                stock: parseInt(document.getElementById('productStock').value),
                timeLeft: parseInt(document.getElementById('productDuration').value) * 3600,
                bids: 0,
                isActive: true,
                sellerId: currentUser.email
            };
            await fetch(`${API_URL}/auctions`, {
                method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(newProduct)
            });
            showNotification('Product added!');
            bootstrap.Modal.getInstance(document.getElementById('addProductModal')).hide();
            document.getElementById('addProductForm').reset();
            renderSellerAuctions();
            updateSellerStats();
        };
        reader.readAsDataURL(imageFile);
    });

    document.getElementById('addFundsForm').addEventListener('submit', async function(e) {
        e.preventDefault();
        const amount = parseInt(document.getElementById('fundsAmount').value);
        await fetch(`${API_URL}/users/wallet`, {
            method: 'POST', headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: currentUser.email, amount })
        });
        updateBuyerWallet();
        showNotification(`₹${amount} added to wallet!`);
        bootstrap.Modal.getInstance(document.getElementById('addFundsModal')).hide();
        this.reset();
    });


    setTimeout(() => {
        document.getElementById('loading-screen').style.display = 'none';
        document.getElementById('main-content').style.display = 'block';
        loadTheme();
        checkAuth();
    }, 3000);

    setInterval(updateTimers, 1000);
});
