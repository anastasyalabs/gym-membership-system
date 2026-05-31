// Real backend API integration for Gym Membership System.
// This file connects the mobile frontend to the Spring Boot API described by the backend team.

const API_BASE_URL = "";

let authSession = readAuthSession();

let currentUser = null;
let plans = [];
let sessions = [];
let subscriptions = [];
let registrations = [];

// Attendance/check-in is not available in the current backend API contract.
let visitRecords = [];

function readAuthSession() {
    try {
        const stored = localStorage.getItem("gymAuthSession");
        return stored ? JSON.parse(stored) : null;
    } catch (error) {
        console.error("Could not read auth session", error);
        return null;
    }
}

function saveAuthSession(session) {
    authSession = session;
    localStorage.setItem("gymAuthSession", JSON.stringify(session));
}

function clearAuthSession() {
    authSession = null;
    localStorage.removeItem("gymAuthSession");
}

async function apiRequest(url, options = {}) {
    const response = await fetch(`${API_BASE_URL}${url}`, options);
    const contentType = response.headers.get("content-type") || "";

    let payload = null;

    if (contentType.includes("application/json")) {
        payload = await response.json();
    } else {
        payload = await response.text();
    }

    if (!response.ok) {
        const message =
            payload && typeof payload === "object" && payload.message
                ? payload.message
                : payload || `Request failed: ${response.status}`;

        throw new Error(message);
    }

    return payload;
}

function apiGet(url) {
    return apiRequest(url);
}

function apiPost(url, body = null) {
    const options = {
        method: "POST",
        headers: {}
    };

    if (body !== null) {
        options.headers["Content-Type"] = "application/json";
        options.body = JSON.stringify(body);
    }

    return apiRequest(url, options);
}

function apiPut(url, body = null) {
    const options = {
        method: "PUT",
        headers: {}
    };

    if (body !== null) {
        options.headers["Content-Type"] = "application/json";
        options.body = JSON.stringify(body);
    }

    return apiRequest(url, options);
}

function apiDelete(url) {
    return apiRequest(url, { method: "DELETE" });
}

function encodeEmail() {
    if (!authSession || !authSession.email) {
        return "";
    }

    return encodeURIComponent(authSession.email);
}

function formatDateTimePart(dateTime, part) {
    if (!dateTime) {
        return "—";
    }

    const [date, time] = dateTime.split("T");

    if (part === "date") {
        return date;
    }

    if (!time) {
        return "—";
    }

    return time.slice(0, 5);
}

function normalizePlan(apiPlan) {
    return {
        id: apiPlan.id,
        membershipType: apiPlan.membershipType,
        name: apiPlan.name,
        price: apiPlan.price,
        duration: `${apiPlan.durationDays} days`,
        durationDays: apiPlan.durationDays,
        description: apiPlan.description,
        active: apiPlan.active,
        popular: apiPlan.membershipType === "PREMIUM" || apiPlan.name?.toLowerCase().includes("premium"),
        features: buildPlanFeatures(apiPlan)
    };
}

function buildPlanFeatures(apiPlan) {
    if (apiPlan.membershipType === "PREMIUM") {
        return ["Gym equipment", "Group classes", "Priority booking"];
    }

    return ["Gym equipment", "Standard access"];
}

function normalizeSession(apiSession) {
    const isRegistered = registrations.some(registration =>
        registration.sessionId === apiSession.id && registration.status === "REGISTERED"
    );

    const registeredCount = Number(apiSession.registeredCount || 0);
    const availablePlaces =
        typeof apiSession.availablePlaces === "number"
            ? apiSession.availablePlaces
            : Math.max(0, apiSession.capacity - registeredCount);

    return {
        id: apiSession.id,
        title: apiSession.title,
        trainer: apiSession.trainerName,
        date: formatDateTimePart(apiSession.startDatetime, "date"),
        time: formatDateTimePart(apiSession.startDatetime, "time"),
        endTime: formatDateTimePart(apiSession.endDatetime, "time"),
        capacity: apiSession.capacity,
        booked: Math.max(0, apiSession.capacity - availablePlaces),
        availablePlaces,
        status: apiSession.status,
        registered: isRegistered
    };
}

function getActiveSubscription() {
    if (!Array.isArray(subscriptions) || subscriptions.length === 0) {
        return null;
    }

    const activeSubscription = subscriptions.find(subscription => subscription.status === "ACTIVE");

    if (activeSubscription) {
        return activeSubscription;
    }

    return subscriptions[0];
}

function buildCurrentUser(profile) {
    const currentSubscription = getActiveSubscription();

    return {
        id: profile.id,
        firstName: profile.name,
        fullName: `${profile.name || ""} ${profile.surname || ""}`.trim(),
        email: profile.email,
        phone: profile.phone,
        role: authSession?.role || "MEMBER",
        status: currentSubscription ? currentSubscription.status : "NO PLAN",
        accountStatus: profile.status,
        plan: currentSubscription ? currentSubscription.planName : "No plan",
        subscriptionEndDate: currentSubscription ? currentSubscription.endDate : null
    };
}

async function loadPublicData() {
    const [plansData, sessionsData] = await Promise.all([
        apiGet("/api/public/plans"),
        apiGet("/api/public/sessions")
    ]);

    plans = plansData.map(normalizePlan);
    sessions = sessionsData.map(normalizeSession);
}

async function loadMemberData() {
    if (!authSession || !authSession.email) {
        return;
    }

    const email = encodeEmail();

    const [profileData, subscriptionData, registrationData] = await Promise.all([
        apiGet(`/api/member/profile?email=${email}`),
        apiGet(`/api/member/subscriptions/my?email=${email}`),
        apiGet(`/api/member/registrations/my?email=${email}`)
    ]);

    subscriptions = Array.isArray(subscriptionData) ? subscriptionData : [];
    registrations = Array.isArray(registrationData) ? registrationData : [];
    currentUser = buildCurrentUser(profileData);

    const sessionsData = await apiGet("/api/public/sessions");
    sessions = sessionsData.map(normalizeSession);
}

async function loadInitialData() {
    await loadPublicData();

    if (authSession) {
        await loadMemberData();
    }
}

function getCurrentPlan() {
    const currentPlan = plans.find(plan => plan.name === currentUser?.plan);

    if (currentPlan) {
        return currentPlan;
    }

    return {
        id: null,
        name: "No plan",
        price: 0,
        duration: "0 days",
        features: []
    };
}

function formatLocalDate(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
}

function getTodayDate() {
    return formatLocalDate(new Date());
}

function getStatusClasses(status) {
    if (status === "ACTIVE") {
        return "bg-emerald-100 text-emerald-700";
    }

    if (status === "EXPIRED") {
        return "bg-orange-100 text-orange-700";
    }

    if (status === "CANCELLED") {
        return "bg-rose-100 text-rose-700";
    }

    return "bg-slate-200 text-slate-700";
}

function getStatusBadge(status) {
    return `<span class="rounded-full px-3 py-1 text-xs font-black ${getStatusClasses(status)}">${status}</span>`;
}

function getMonthName(date) {
    return date.toLocaleString("en", { month: "long", year: "numeric" });
}

function getFormattedExpiryDate() {
    if (!currentUser || !currentUser.subscriptionEndDate) {
        return "—";
    }

    const date = new Date(`${currentUser.subscriptionEndDate}T00:00:00`);

    return date.toLocaleDateString("en", {
        day: "numeric",
        month: "short"
    });
}

function showToast(message) {
    const toast = document.getElementById("toast");

    if (!toast) {
        alert(message);
        return;
    }

    toast.textContent = message;
    toast.classList.remove("hidden");

    setTimeout(() => {
        toast.classList.add("hidden");
    }, 2600);
}

function setActivePage(pageName) {
    if (!authSession && pageName !== "home") {
        showAuthOverlay();
        return;
    }

    const pages = document.querySelectorAll(".app-page");
    const navButtons = document.querySelectorAll(".nav-button");

    pages.forEach(page => page.classList.add("hidden"));

    navButtons.forEach(button => {
        button.classList.remove("bg-slate-950", "text-white");
        button.classList.add("text-slate-400");
    });

    const activePage = document.getElementById(`${pageName}Page`);
    const activeButton = document.querySelector(`.nav-button[data-page="${pageName}"]`);

    if (activePage) {
        activePage.classList.remove("hidden");
    }

    if (activeButton) {
        activeButton.classList.add("bg-slate-950", "text-white");
        activeButton.classList.remove("text-slate-400");
    }

    window.scrollTo({ top: 0, behavior: "smooth" });
}

function renderGuestState() {
    document.getElementById("headerUserName").textContent = "Guest";
    document.getElementById("heroPlanName").textContent = "No plan";
    document.getElementById("heroStatusBadge").textContent = "GUEST";
    document.getElementById("heroVisitCount").textContent = "0";
    document.getElementById("heroRegisteredClasses").textContent = "0";
    document.getElementById("heroPlanExpires").textContent = "—";

    document.getElementById("monthVisitCount").textContent = "0";
    document.getElementById("nextClassShort").textContent = sessions[0] ? sessions[0].time : "—";

    document.getElementById("homePlanName").textContent = "No active plan";
    document.getElementById("homePlanDetails").textContent = "Login to choose a membership plan";
    document.getElementById("homeStatusBadge").innerHTML = getStatusBadge("GUEST");

    document.getElementById("accountName").textContent = "Guest";
    document.getElementById("accountEmail").textContent = "Please log in";
    document.getElementById("accountStatusBadge").innerHTML = getStatusBadge("GUEST");

    document.getElementById("adminShortcutCard").classList.add("hidden");
    document.getElementById("adminPanelButton").classList.add("hidden");

    renderDashboard();
    renderClasses();
    renderAccountPlans();
    renderCheckInPage();
    renderCalendar();
}

function renderHeader() {
    if (!currentUser) {
        renderGuestState();
        return;
    }

    const plan = getCurrentPlan();
    const visitsThisMonth = getVisitsForCurrentMonth().length;
    const registeredClasses = registrations.filter(registration => registration.status === "REGISTERED").length;

    document.getElementById("headerUserName").textContent = currentUser.firstName;
    document.getElementById("heroPlanName").textContent = currentUser.plan;
    document.getElementById("heroStatusBadge").textContent = currentUser.status;
    document.getElementById("heroVisitCount").textContent = visitsThisMonth;
    document.getElementById("heroRegisteredClasses").textContent = registeredClasses;
    document.getElementById("heroPlanExpires").textContent = getFormattedExpiryDate();

    document.getElementById("homePlanName").textContent = currentUser.plan;
    document.getElementById("homePlanDetails").textContent =
        currentUser.plan === "No plan" ? "Choose a membership plan" : `€${plan.price} / ${plan.duration}`;

    document.getElementById("homeStatusBadge").innerHTML = getStatusBadge(currentUser.status);

    document.getElementById("monthVisitCount").textContent = visitsThisMonth;
    document.getElementById("accountName").textContent = currentUser.fullName;
    document.getElementById("accountEmail").textContent = currentUser.email;
    document.getElementById("accountStatusBadge").innerHTML = getStatusBadge(currentUser.status);

    const isAdmin = currentUser.role === "ADMIN";

    document.getElementById("adminShortcutCard").classList.toggle("hidden", !isAdmin);
    document.getElementById("adminPanelButton").classList.toggle("hidden", !isAdmin);
}

function renderDashboard() {
    const nextSession = sessions.find(session => !session.registered && session.status === "AVAILABLE") || sessions[0];

    document.getElementById("nextClassShort").textContent = nextSession ? nextSession.time : "—";

    if (!nextSession) {
        document.getElementById("nextSessionCard").innerHTML = `
            <p class="rounded-3xl bg-slate-100 p-4 text-sm text-slate-500">No upcoming sessions.</p>
        `;
        return;
    }

    document.getElementById("nextSessionCard").innerHTML = `
        <div class="rounded-3xl bg-slate-100 p-4">
            <div class="flex items-center justify-between gap-4">
                <div>
                    <p class="font-black text-slate-950">${nextSession.title}</p>
                    <p class="mt-1 text-sm text-slate-500">${nextSession.date} · ${nextSession.time}</p>
                    <p class="mt-1 text-sm text-slate-500">Trainer: ${nextSession.trainer}</p>
                </div>

                <div class="flex h-14 w-14 items-center justify-center rounded-2xl bg-violet-500 text-xl font-black text-white">
                    ${nextSession.availablePlaces}
                </div>
            </div>
        </div>
    `;
}

function renderCheckInPage() {
    const today = new Date();

    document.getElementById("checkinDateLabel").textContent =
        today.toLocaleDateString("en", { weekday: "long", day: "numeric", month: "long" });

    const message = document.getElementById("checkinMessage");

    if (message) {
        message.classList.remove("hidden", "bg-emerald-100", "text-emerald-700");
        message.classList.add("bg-orange-100", "text-orange-700");
        message.textContent = "Check-in by code is not available yet because the backend API does not include attendance/access-code endpoints.";
    }

    renderRecentVisits();
}

function renderRecentVisits() {
    const recentVisitsList = document.getElementById("recentVisitsList");

    recentVisitsList.innerHTML = `
        <p class="rounded-2xl bg-slate-100 p-4 text-sm font-semibold text-slate-500">
            Visit history is not connected yet. Backend needs attendance endpoints.
        </p>
    `;
}

function getVisitsForCurrentMonth() {
    return [];
}

function renderCalendar() {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);

    document.getElementById("calendarTitle").textContent = getMonthName(now);

    const cells = [];

    for (let i = 0; i < firstDay.getDay(); i++) {
        cells.push(`<div></div>`);
    }

    for (let day = 1; day <= lastDay.getDate(); day++) {
        const date = new Date(year, month, day);
        const dateKey = formatLocalDate(date);
        const isToday = dateKey === getTodayDate();

        cells.push(`
            <div class="relative flex h-11 items-center justify-center rounded-2xl bg-white text-sm font-black text-slate-700
                        ${isToday ? "ring-2 ring-violet-400" : ""}">
                ${day}
            </div>
        `);
    }

    document.getElementById("calendarGrid").innerHTML = cells.join("");
    renderMonthVisitsList();
}

function renderMonthVisitsList() {
    const monthVisitsList = document.getElementById("monthVisitsList");

    monthVisitsList.innerHTML = `
        <p class="rounded-2xl bg-slate-100 p-4 text-sm font-semibold text-slate-500">
            Calendar is ready in the UI, but visit records require backend attendance API.
        </p>
    `;
}

function getSessionButtonText(session, isFull, isInactive, isUnavailable) {
    if (session.registered) {
        return "Registered";
    }

    if (isUnavailable) {
        return "Unavailable";
    }

    if (isFull) {
        return "Session full";
    }

    if (isInactive) {
        return "Subscription inactive";
    }

    return "Register";
}

function renderClasses() {
    const classesList = document.getElementById("classesList");

    if (!classesList) {
        return;
    }

    if (sessions.length === 0) {
        classesList.innerHTML = `
            <p class="rounded-2xl bg-white p-4 text-sm font-semibold text-slate-500">
                No available training sessions.
            </p>
        `;
        return;
    }

    classesList.innerHTML = sessions.map(session => {
        const availablePlaces = session.availablePlaces;
        const isFull = availablePlaces <= 0;
        const isInactive = !currentUser || currentUser.status !== "ACTIVE";
        const isUnavailable = session.status !== "AVAILABLE";
        const isDisabled = session.registered || isFull || isInactive || isUnavailable;
        const progress = Math.min(100, Math.round((session.booked / session.capacity) * 100));

        return `
            <article class="rounded-[2rem] bg-white p-5 shadow-lg shadow-violet-100">
                <div class="mb-4 flex items-start justify-between gap-4">
                    <div>
                        <h3 class="text-lg font-black text-slate-950">${session.title}</h3>
                        <p class="mt-1 text-sm text-slate-500">${session.date} · ${session.time}</p>
                        <p class="mt-1 text-sm text-slate-500">Trainer: ${session.trainer}</p>
                    </div>

                    <div class="rounded-2xl bg-violet-100 px-3 py-2 text-center">
                        <p class="text-xs font-bold text-violet-500">Free</p>
                        <p class="text-xl font-black text-violet-700">${availablePlaces}</p>
                    </div>
                </div>

                <div class="mb-4 h-3 overflow-hidden rounded-full bg-slate-100">
                    <div class="h-full rounded-full bg-gradient-to-r from-violet-500 to-fuchsia-400" style="width: ${progress}%"></div>
                </div>

                <button class="session-register-button w-full rounded-2xl px-4 py-4 text-sm font-black
                               ${isDisabled ? "bg-slate-200 text-slate-400" : "bg-slate-950 text-white"}"
                        ${isDisabled ? "disabled" : ""}
                        data-session-id="${session.id}">
                    ${getSessionButtonText(session, isFull, isInactive, isUnavailable)}
                </button>

                ${isInactive ? `<p class="mt-3 text-center text-xs font-semibold text-orange-500">Active subscription is required.</p>` : ""}
            </article>
        `;
    }).join("");
}

function renderAccountPlans() {
    const accountPlansList = document.getElementById("accountPlansList");

    if (!accountPlansList) {
        return;
    }

    if (plans.length === 0) {
        accountPlansList.innerHTML = `
            <p class="rounded-2xl bg-slate-100 p-4 text-sm font-semibold text-slate-500">
                No active membership plans.
            </p>
        `;
        return;
    }

    accountPlansList.innerHTML = plans.map(plan => {
        const isCurrent = currentUser && plan.name === currentUser.plan;

        return `
            <article class="rounded-[2rem] ${isCurrent ? "bg-slate-950 text-white" : "bg-slate-100 text-slate-950"} p-4">
                <div class="flex items-start justify-between gap-4">
                    <div>
                        <div class="flex items-center gap-2">
                            <h4 class="text-lg font-black">${plan.name}</h4>
                            ${plan.popular ? `<span class="rounded-full bg-violet-100 px-2 py-1 text-[10px] font-black text-violet-700">POPULAR</span>` : ""}
                        </div>

                        <p class="mt-1 text-sm ${isCurrent ? "text-white/60" : "text-slate-500"}">${plan.description}</p>

                        <div class="mt-3 flex flex-wrap gap-2">
                            ${plan.features.map(feature => `
                                <span class="rounded-full ${isCurrent ? "bg-white/10 text-white/80" : "bg-white text-slate-500"} px-3 py-1 text-xs font-bold">
                                    ${feature}
                                </span>
                            `).join("")}
                        </div>
                    </div>

                    <div class="text-right">
                        <p class="text-2xl font-black">€${plan.price}</p>
                        <p class="text-xs ${isCurrent ? "text-white/50" : "text-slate-400"}">${plan.duration}</p>
                    </div>
                </div>

                <button class="plan-change-button mt-4 w-full rounded-2xl px-4 py-3 text-sm font-black
                               ${isCurrent ? "bg-white/10 text-white" : "bg-violet-600 text-white"}"
                        ${isCurrent ? "disabled" : ""}
                        data-plan-id="${plan.id}"
                        data-plan-name="${plan.name}">
                    ${isCurrent ? "Current plan" : "Switch to this plan"}
                </button>
            </article>
        `;
    }).join("");
}

function updateAdminCodeDisplay() {
    const codeElement = document.getElementById("adminCurrentCode");
    const timerLabel = document.getElementById("codeTimerLabel");
    const timerBar = document.getElementById("codeTimerBar");

    if (codeElement) {
        codeElement.textContent = "N/A";
    }

    if (timerLabel) {
        timerLabel.textContent = "not connected";
    }

    if (timerBar) {
        timerBar.style.width = "0%";
    }
}

function renderAdminAttendance() {
    const adminAttendanceList = document.getElementById("adminAttendanceList");

    if (!adminAttendanceList) {
        return;
    }

    adminAttendanceList.innerHTML = `
        <p class="rounded-2xl bg-slate-100 p-4 text-sm font-semibold text-slate-500">
            Admin attendance view requires attendance backend endpoints.
        </p>
    `;
}

async function processCheckIn() {
    const message = document.getElementById("checkinMessage");

    message.classList.remove("hidden", "bg-emerald-100", "text-emerald-700", "bg-rose-100", "text-rose-700");
    message.classList.add("bg-orange-100", "text-orange-700");
    message.textContent = "Check-in is not connected yet. Backend must add attendance/access-code endpoints.";
}

async function registerForSession(sessionId) {
    if (!authSession) {
        showAuthOverlay();
        return;
    }

    try {
        await apiPost(`/api/member/registrations?email=${encodeEmail()}&sessionId=${encodeURIComponent(sessionId)}`);

        showToast("Registered for training session");

        await loadMemberData();
        refreshAll();
    } catch (error) {
        showToast(error.message || "Could not register for session");
    }
}

async function switchPlan(planId, planName) {
    if (!authSession) {
        showAuthOverlay();
        return;
    }

    try {
        await apiPost(`/api/member/subscriptions?email=${encodeEmail()}&planId=${encodeURIComponent(planId)}`);

        showToast(`Plan changed to ${planName}`);

        await loadMemberData();
        refreshAll();
    } catch (error) {
        showToast(error.message || "Could not change plan");
    }
}

function refreshAll() {
    renderHeader();
    renderDashboard();
    renderCheckInPage();
    renderCalendar();
    renderClasses();
    renderAccountPlans();
    renderAdminAttendance();
    updateAdminCodeDisplay();
}

function createAuthOverlay() {
    if (document.getElementById("authOverlay")) {
        return;
    }

    const overlay = document.createElement("div");
    overlay.id = "authOverlay";
    overlay.className = "fixed inset-0 z-50 flex items-center justify-center bg-slate-950/95 p-5";

    overlay.innerHTML = `
        <div class="w-full max-w-[430px] rounded-[2rem] bg-[#f8f3ff] p-5 shadow-2xl">
            <div class="mb-5 flex items-center justify-between">
                <div>
                    <p class="text-sm text-slate-500">Gym Membership System</p>
                    <h2 class="text-2xl font-black text-slate-950" id="authTitle">Login</h2>
                </div>

                <div class="flex h-11 w-11 items-center justify-center rounded-full bg-white shadow-lg">
                    <span class="font-black text-violet-600">G</span>
                </div>
            </div>

            <div class="mb-4 grid grid-cols-2 gap-2 rounded-2xl bg-white p-2">
                <button id="showLoginButton" class="rounded-xl bg-slate-950 px-4 py-3 text-sm font-black text-white">Login</button>
                <button id="showRegisterButton" class="rounded-xl px-4 py-3 text-sm font-black text-slate-500">Register</button>
            </div>

            <form id="loginForm" class="space-y-3">
                <input id="loginEmail" class="w-full rounded-2xl bg-white px-4 py-4 text-sm outline-none focus:ring-2 focus:ring-violet-300"
                       type="email" placeholder="Email" value="anna@gym.com">

                <input id="loginPassword" class="w-full rounded-2xl bg-white px-4 py-4 text-sm outline-none focus:ring-2 focus:ring-violet-300"
                       type="password" placeholder="Password" value="anna123">

                <button class="w-full rounded-2xl bg-violet-600 px-4 py-4 text-sm font-black text-white" type="submit">
                    Login
                </button>
            </form>

            <form id="registerForm" class="hidden space-y-3">
                <input id="registerEmail" class="w-full rounded-2xl bg-white px-4 py-4 text-sm outline-none focus:ring-2 focus:ring-violet-300"
                       type="email" placeholder="Email">

                <input id="registerPassword" class="w-full rounded-2xl bg-white px-4 py-4 text-sm outline-none focus:ring-2 focus:ring-violet-300"
                       type="password" placeholder="Password">

                <div class="grid grid-cols-2 gap-3">
                    <input id="registerName" class="w-full rounded-2xl bg-white px-4 py-4 text-sm outline-none focus:ring-2 focus:ring-violet-300"
                           type="text" placeholder="Name">

                    <input id="registerSurname" class="w-full rounded-2xl bg-white px-4 py-4 text-sm outline-none focus:ring-2 focus:ring-violet-300"
                           type="text" placeholder="Surname">
                </div>

                <input id="registerPhone" class="w-full rounded-2xl bg-white px-4 py-4 text-sm outline-none focus:ring-2 focus:ring-violet-300"
                       type="text" placeholder="Phone">

                <input id="registerDateOfBirth" class="w-full rounded-2xl bg-white px-4 py-4 text-sm outline-none focus:ring-2 focus:ring-violet-300"
                       type="date">

                <button class="w-full rounded-2xl bg-violet-600 px-4 py-4 text-sm font-black text-white" type="submit">
                    Create account
                </button>
            </form>

            <p id="authMessage" class="mt-4 hidden rounded-2xl px-4 py-3 text-sm font-bold"></p>
        </div>
    `;

    document.body.appendChild(overlay);

    setupAuthOverlayEvents();
}

function showAuthOverlay() {
    createAuthOverlay();
    document.getElementById("authOverlay").classList.remove("hidden");
}

function hideAuthOverlay() {
    const overlay = document.getElementById("authOverlay");

    if (overlay) {
        overlay.classList.add("hidden");
    }
}

function showAuthMessage(message, type = "error") {
    const messageBox = document.getElementById("authMessage");

    messageBox.textContent = message;
    messageBox.classList.remove("hidden", "bg-rose-100", "text-rose-700", "bg-emerald-100", "text-emerald-700");

    if (type === "success") {
        messageBox.classList.add("bg-emerald-100", "text-emerald-700");
    } else {
        messageBox.classList.add("bg-rose-100", "text-rose-700");
    }
}

function setAuthMode(mode) {
    const isLogin = mode === "login";

    document.getElementById("authTitle").textContent = isLogin ? "Login" : "Register";
    document.getElementById("loginForm").classList.toggle("hidden", !isLogin);
    document.getElementById("registerForm").classList.toggle("hidden", isLogin);

    document.getElementById("showLoginButton").className =
        isLogin
            ? "rounded-xl bg-slate-950 px-4 py-3 text-sm font-black text-white"
            : "rounded-xl px-4 py-3 text-sm font-black text-slate-500";

    document.getElementById("showRegisterButton").className =
        !isLogin
            ? "rounded-xl bg-slate-950 px-4 py-3 text-sm font-black text-white"
            : "rounded-xl px-4 py-3 text-sm font-black text-slate-500";
}

function setupAuthOverlayEvents() {
    document.getElementById("showLoginButton").addEventListener("click", () => setAuthMode("login"));
    document.getElementById("showRegisterButton").addEventListener("click", () => setAuthMode("register"));

    document.getElementById("loginForm").addEventListener("submit", async event => {
        event.preventDefault();

        const email = document.getElementById("loginEmail").value.trim();
        const password = document.getElementById("loginPassword").value;

        try {
            const loginData = await apiPost("/api/public/login", {
                email,
                password
            });

            saveAuthSession(loginData);

            await loadInitialData();
            hideAuthOverlay();
            refreshAll();
            setActivePage("home");

            showToast("Logged in successfully");
        } catch (error) {
            showAuthMessage(error.message || "Login failed");
        }
    });

    document.getElementById("registerForm").addEventListener("submit", async event => {
        event.preventDefault();

        const payload = {
            email: document.getElementById("registerEmail").value.trim(),
            password: document.getElementById("registerPassword").value,
            name: document.getElementById("registerName").value.trim(),
            surname: document.getElementById("registerSurname").value.trim(),
            phone: document.getElementById("registerPhone").value.trim(),
            dateOfBirth: document.getElementById("registerDateOfBirth").value
        };

        try {
            await apiPost("/api/public/register", payload);

            showAuthMessage("Account created successfully. You can now log in.", "success");
            setAuthMode("login");
            document.getElementById("loginEmail").value = payload.email;
            document.getElementById("loginPassword").value = payload.password;
        } catch (error) {
            showAuthMessage(error.message || "Registration failed");
        }
    });
}

function setupEvents() {
    document.querySelectorAll(".nav-button").forEach(button => {
        button.addEventListener("click", () => {
            setActivePage(button.dataset.page);
        });
    });

    document.addEventListener("click", event => {
        const goButton = event.target.closest("[data-go]");

        if (goButton) {
            setActivePage(goButton.dataset.go);
        }
    });

    document.getElementById("checkinSubmitButton").addEventListener("click", processCheckIn);

    document.getElementById("checkinCodeInput").addEventListener("input", event => {
        event.target.value = event.target.value.replace(/\D/g, "").slice(0, 6);
    });

    const roleSelect = document.getElementById("roleSelect");

    if (roleSelect) {
        roleSelect.disabled = true;
        roleSelect.title = "Role comes from backend login response";
    }

    document.addEventListener("click", event => {
        const sessionButton = event.target.closest(".session-register-button");

        if (sessionButton && !sessionButton.disabled) {
            registerForSession(sessionButton.dataset.sessionId);
        }

        const planButton = event.target.closest(".plan-change-button");

        if (planButton && !planButton.disabled) {
            switchPlan(planButton.dataset.planId, planButton.dataset.planName);
        }
    });
}

async function initializeApp() {
    try {
        setupEvents();

        await loadInitialData();
        refreshAll();
        setActivePage("home");

        if (!authSession) {
            showAuthOverlay();
        } else {
            const roleSelect = document.getElementById("roleSelect");
            if (roleSelect) {
                roleSelect.value = authSession.role;
            }
        }
    } catch (error) {
        console.error(error);
        showToast(error.message || "Could not load application data");

        if (!authSession) {
            showAuthOverlay();
        }
    }
}

initializeApp();
