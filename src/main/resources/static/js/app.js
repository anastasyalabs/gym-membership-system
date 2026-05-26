// Frontend integration layer.
// The UI now loads data from Spring Boot REST endpoints instead of hardcoded frontend arrays.

let currentUser = null;
let plans = [];
let sessions = [];
let visitRecords = [];
let selectedUiRole = "MEMBER";

async function apiGet(url) {
    const response = await fetch(url);

    if (!response.ok) {
        throw new Error(`GET request failed: ${url}`);
    }

    return response.json();
}

async function apiPost(url, body = {}) {
    const response = await fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
    });

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || `POST request failed: ${url}`);
    }

    return response.json();
}

async function loadInitialData() {
    const [memberData, planData, sessionData, visitData] = await Promise.all([
        apiGet("/api/member/me"),
        apiGet("/api/plans"),
        apiGet("/api/sessions"),
        apiGet("/api/member/me/visits")
    ]);

    currentUser = {
        ...memberData,
        role: selectedUiRole || memberData.role
    };

    plans = planData;
    sessions = sessionData;
    visitRecords = visitData;
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

function getCurrentPlan() {
    return plans.find(plan => plan.name === currentUser.plan) || plans[0] || {
        name: "No plan",
        price: 0,
        duration: "0 days",
        features: []
    };
}

function getStatusClasses(status) {
    if (status === "ACTIVE") {
        return "bg-emerald-100 text-emerald-700";
    }

    if (status === "EXPIRED") {
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
    if (!currentUser.subscriptionEndDate) {
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

    toast.textContent = message;
    toast.classList.remove("hidden");

    setTimeout(() => {
        toast.classList.add("hidden");
    }, 2400);
}

function setActivePage(pageName) {
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

function renderHeader() {
    const plan = getCurrentPlan();
    const visitsThisMonth = getVisitsForCurrentMonth().length;
    const registeredClasses = sessions.filter(session => session.registered).length;

    document.getElementById("headerUserName").textContent = currentUser.firstName;
    document.getElementById("heroPlanName").textContent = currentUser.plan;
    document.getElementById("heroStatusBadge").textContent = currentUser.status;
    document.getElementById("heroVisitCount").textContent = visitsThisMonth;
    document.getElementById("heroRegisteredClasses").textContent = registeredClasses;
    document.getElementById("heroPlanExpires").textContent = getFormattedExpiryDate();

    document.getElementById("homePlanName").textContent = currentUser.plan;
    document.getElementById("homePlanDetails").textContent = `€${plan.price} / ${plan.duration}`;
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
    const nextSession = sessions.find(session => !session.registered) || sessions[0];

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
                    ${nextSession.capacity - nextSession.booked}
                </div>
            </div>
        </div>
    `;
}

function renderCheckInPage() {
    const today = new Date();

    document.getElementById("checkinDateLabel").textContent =
        today.toLocaleDateString("en", { weekday: "long", day: "numeric", month: "long" });

    renderRecentVisits();
}

function renderRecentVisits() {
    const recentVisitsList = document.getElementById("recentVisitsList");
    const recentVisits = [...visitRecords].reverse().slice(0, 4);

    if (recentVisits.length === 0) {
        recentVisitsList.innerHTML = `<p class="text-sm text-slate-500">No check-ins yet.</p>`;
        return;
    }

    recentVisitsList.innerHTML = recentVisits.map(visit => `
        <div class="flex items-center justify-between rounded-2xl bg-slate-100 p-3">
            <div>
                <p class="text-sm font-black text-slate-950">${visit.date}</p>
                <p class="text-xs text-slate-500">Gym entrance confirmed</p>
            </div>

            <span class="rounded-full bg-violet-100 px-3 py-1 text-xs font-black text-violet-700">${visit.time}</span>
        </div>
    `).join("");
}

function getVisitsForCurrentMonth() {
    const now = new Date();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();

    return visitRecords.filter(visit => {
        const date = new Date(`${visit.date}T00:00:00`);
        return date.getMonth() === currentMonth && date.getFullYear() === currentYear;
    });
}

function renderCalendar() {
    const now = new Date();
    const year = now.getFullYear();
    const month = now.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const visitDates = new Set(visitRecords.map(visit => visit.date));

    document.getElementById("calendarTitle").textContent = getMonthName(now);

    const cells = [];

    for (let i = 0; i < firstDay.getDay(); i++) {
        cells.push(`<div></div>`);
    }

    for (let day = 1; day <= lastDay.getDate(); day++) {
        const date = new Date(year, month, day);
        const dateKey = formatLocalDate(date);
        const hasVisit = visitDates.has(dateKey);
        const isToday = dateKey === getTodayDate();

        cells.push(`
            <div class="relative flex h-11 items-center justify-center rounded-2xl text-sm font-black
                        ${hasVisit ? "bg-violet-600 text-white" : "bg-white text-slate-700"}
                        ${isToday && !hasVisit ? "ring-2 ring-violet-400" : ""}">
                ${day}
                ${hasVisit ? `<span class="absolute bottom-1 h-1.5 w-1.5 rounded-full bg-white"></span>` : ""}
            </div>
        `);
    }

    document.getElementById("calendarGrid").innerHTML = cells.join("");
    renderMonthVisitsList();
}

function renderMonthVisitsList() {
    const monthVisitsList = document.getElementById("monthVisitsList");
    const monthVisits = getVisitsForCurrentMonth();

    if (monthVisits.length === 0) {
        monthVisitsList.innerHTML = `<p class="text-sm text-slate-500">No visits this month.</p>`;
        return;
    }

    monthVisitsList.innerHTML = [...monthVisits].reverse().map(visit => `
        <div class="flex items-center justify-between rounded-2xl bg-slate-100 p-3">
            <div>
                <p class="text-sm font-black text-slate-950">${visit.date}</p>
                <p class="text-xs text-slate-500">Recorded check-in</p>
            </div>

            <span class="rounded-full bg-emerald-100 px-3 py-1 text-xs font-black text-emerald-700">${visit.time}</span>
        </div>
    `).join("");
}

function getSessionButtonText(session, isFull, isInactive) {
    if (session.registered) {
        return "Registered";
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

    classesList.innerHTML = sessions.map(session => {
        const availablePlaces = session.capacity - session.booked;
        const isFull = availablePlaces <= 0;
        const isInactive = currentUser.status !== "ACTIVE";
        const isDisabled = session.registered || isFull || isInactive;
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
                    ${getSessionButtonText(session, isFull, isInactive)}
                </button>

                ${isInactive ? `<p class="mt-3 text-center text-xs font-semibold text-rose-500">Only ACTIVE members can register.</p>` : ""}
            </article>
        `;
    }).join("");
}

function renderAccountPlans() {
    const accountPlansList = document.getElementById("accountPlansList");

    accountPlansList.innerHTML = plans.map(plan => {
        const isCurrent = plan.name === currentUser.plan;

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
                        data-plan-name="${plan.name}">
                    ${isCurrent ? "Current plan" : "Switch to this plan"}
                </button>
            </article>
        `;
    }).join("");
}

async function updateAdminCodeDisplay() {
    try {
        const data = await apiGet("/api/admin/access-code/current");

        const codeElement = document.getElementById("adminCurrentCode");
        const timerLabel = document.getElementById("codeTimerLabel");
        const timerBar = document.getElementById("codeTimerBar");

        if (codeElement) {
            codeElement.textContent = data.code;
        }

        if (timerLabel) {
            timerLabel.textContent = `${data.remainingSeconds}s`;
        }

        if (timerBar) {
            timerBar.style.width = `${(data.remainingSeconds / 60) * 100}%`;
        }
    } catch (error) {
        console.error(error);
    }
}

function renderAdminAttendance() {
    const adminAttendanceList = document.getElementById("adminAttendanceList");
    const today = getTodayDate();
    const todayVisits = visitRecords.filter(visit => visit.date === today);

    if (todayVisits.length === 0) {
        adminAttendanceList.innerHTML = `
            <p class="rounded-2xl bg-slate-100 p-4 text-sm font-semibold text-slate-500">
                No check-ins recorded today.
            </p>
        `;
        return;
    }

    adminAttendanceList.innerHTML = todayVisits.map(visit => `
        <div class="flex items-center justify-between rounded-2xl bg-slate-100 p-3">
            <div>
                <p class="text-sm font-black text-slate-950">${currentUser.fullName}</p>
                <p class="text-xs text-slate-500">Checked in today</p>
            </div>

            <span class="rounded-full bg-emerald-100 px-3 py-1 text-xs font-black text-emerald-700">${visit.time}</span>
        </div>
    `).join("");
}

async function processCheckIn() {
    const input = document.getElementById("checkinCodeInput");
    const message = document.getElementById("checkinMessage");
    const enteredCode = input.value.trim();

    message.classList.remove("hidden", "bg-emerald-100", "text-emerald-700", "bg-rose-100", "text-rose-700");

    try {
        const response = await apiPost("/api/check-in", { code: enteredCode });

        visitRecords = response.visits;
        message.textContent = response.message;

        if (response.success) {
            message.classList.add("bg-emerald-100", "text-emerald-700");
            input.value = "";
            showToast(response.message);
        } else {
            message.classList.add("bg-rose-100", "text-rose-700");
        }

        refreshAll();
    } catch (error) {
        message.textContent = error.message || "Check-in failed.";
        message.classList.add("bg-rose-100", "text-rose-700");
    }
}

async function registerForSession(sessionId) {
    try {
        const updatedSession = await apiPost(`/api/sessions/${sessionId}/register`);

        sessions = sessions.map(session =>
            session.id === updatedSession.id ? updatedSession : session
        );

        refreshAll();
        showToast(`Registered for ${updatedSession.title}`);
    } catch (error) {
        showToast(error.message || "Could not register for session");
    }
}

async function switchPlan(planName) {
    try {
        const updatedMember = await apiPost("/api/member/me/change-plan", { planName });

        currentUser = {
            ...updatedMember,
            role: selectedUiRole
        };

        refreshAll();
        showToast(`Plan changed to ${planName}`);
    } catch (error) {
        showToast(error.message || "Could not change plan");
    }
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

    document.getElementById("roleSelect").addEventListener("change", event => {
        selectedUiRole = event.target.value;
        currentUser.role = selectedUiRole;
        refreshAll();

        if (currentUser.role === "ADMIN") {
            showToast("Admin mode enabled");
        } else {
            showToast("Member mode enabled");
            setActivePage("home");
        }
    });

    document.addEventListener("click", event => {
        const sessionButton = event.target.closest(".session-register-button");

        if (sessionButton && !sessionButton.disabled) {
            registerForSession(sessionButton.dataset.sessionId);
        }

        const planButton = event.target.closest(".plan-change-button");

        if (planButton && !planButton.disabled) {
            switchPlan(planButton.dataset.planName);
        }
    });
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

async function initializeApp() {
    try {
        await loadInitialData();

        selectedUiRole = currentUser.role;
        document.getElementById("roleSelect").value = selectedUiRole;

        setupEvents();
        refreshAll();
        setActivePage("home");

        setInterval(updateAdminCodeDisplay, 1000);
    } catch (error) {
        console.error(error);
        showToast("Could not load application data from backend");
    }
}

initializeApp();
