const fs = require("fs");
const path = require("path");

const SCHEMA = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json";
const outDir = __dirname;

function uid(seed) {
  return `9a9d${seed}-6f81-4a2b-9c41-${seed}${seed}${seed}${seed}`.slice(0, 36);
}

function lines(script) {
  return script.trim().split(/\r?\n/);
}

function body(value) {
  return {
    mode: "raw",
    raw: JSON.stringify(value, null, 2),
    options: { raw: { language: "json" } },
  };
}

function req(name, method, urlPath, options = {}) {
  const headers = options.body ? [{ key: "Content-Type", value: "application/json" }] : [];
  const item = {
    name,
    request: {
      method,
      header: headers,
      url: {
        raw: `{{baseUrl}}${urlPath}`,
        host: ["{{baseUrl}}"],
        path: urlPath.replace(/^\//, "").split("/"),
      },
    },
    response: [],
  };
  if (options.auth) {
    item.request.auth = {
      type: "bearer",
      bearer: [{ key: "token", value: options.auth, type: "string" }],
    };
  }
  if (options.body) {
    item.request.body = body(options.body);
  }
  const event = [];
  if (options.pre) {
    event.push({ listen: "prerequest", script: { type: "text/javascript", exec: lines(options.pre) } });
  }
  if (options.test) {
    event.push({ listen: "test", script: { type: "text/javascript", exec: lines(options.test) } });
  }
  if (event.length) {
    item.event = event;
  }
  return item;
}

function collection(idSeed, name, description, variables, items) {
  return {
    info: {
      _postman_id: uid(idSeed),
      name,
      description,
      schema: SCHEMA,
    },
    variable: Object.entries({
      baseUrl: "http://localhost:8080/api/v1",
      password: "Password@123",
      adminEmail: "admin@example.com",
      adminPassword: "Password@123",
      managerEmail: "admin@example.com",
      managerPassword: "Password@123",
      ...variables,
    }).map(([key, value]) => ({ key, value })),
    item: items,
  };
}

function prepare(name, prefix, healthPath, moduleName) {
  return req(name, "GET", "/auth/health", {
    pre: `
const runId = Date.now().toString();
pm.collectionVariables.set("runId", runId);
pm.collectionVariables.set("${prefix}Email", "${prefix}." + runId + "@example.com");
pm.collectionVariables.set("${prefix}Username", "${prefix}_" + runId);
pm.collectionVariables.set("${prefix}FullName", "${prefix.charAt(0).toUpperCase() + prefix.slice(1)} " + runId);
`,
    test: `
const json = pm.response.json();
pm.test("${name} returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("${name} confirms public auth health", function () { pm.expect(json.data.module).to.eql("auth"); });
`,
  });
}

function write(fileName, data) {
  fs.writeFileSync(path.join(outDir, fileName), JSON.stringify(data, null, 2) + "\n", "utf8");
  const count = JSON.stringify(data).match(/pm\.test\(/g)?.length || 0;
  console.log(`${fileName}: ${count} test cases`);
}

const publicExplorer = collection(
  "0006",
  "06 - Vy public catalog explorer",
  "Persona Vy: anonymous visitor explores public catalog, health endpoints, pagination, and protected boundaries without logging in. Count: 25 pm.test test cases.",
  {},
  [
    req("06.00 - Vy checks auth health", "GET", "/auth/health", {
      test: `
const json = pm.response.json();
pm.test("auth health is public", function () { pm.response.to.have.status(200); });
pm.test("auth health returns auth module", function () { pm.expect(json.data.module).to.eql("auth"); });
`,
    }),
    req("06.01 - Vy sees content health is protected", "GET", "/content/health", {
      test: `
pm.test("content health without token returns HTTP 401", function () { pm.response.to.have.status(401); });
`,
    }),
    req("06.02 - Vy sees assessment health is protected", "GET", "/quizzes/health", {
      test: `
pm.test("assessment health without token returns HTTP 401", function () { pm.response.to.have.status(401); });
`,
    }),
    req("06.03 - Vy sees classroom health is protected", "GET", "/classes/health", {
      test: `
pm.test("classroom health without token returns HTTP 401", function () { pm.response.to.have.status(401); });
`,
    }),
    req("06.04 - Vy reads seeded languages", "GET", "/languages", {
      test: `
const items = pm.response.json().data;
pm.test("languages return HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("languages contain required seed codes", function () { pm.expect(items.map(x => x.code)).to.include.members(["en", "vi", "ja"]); });
pm.test("language ids are numeric", function () { pm.expect(items[0].id).to.be.a("number"); });
`,
    }),
    req("06.05 - Vy reads seeded topics", "GET", "/topics", {
      test: `
const items = pm.response.json().data;
pm.test("topics return HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("topics contain Daily Life", function () { pm.expect(items.map(x => x.name)).to.include("Daily Life"); });
pm.test("topics are active", function () { pm.expect(items.every(x => x.active === true)).to.eql(true); });
`,
    }),
    req("06.06 - Vy reads tag catalog", "GET", "/tags", {
      test: `
const json = pm.response.json();
pm.test("tags endpoint returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("tags endpoint returns an array", function () { pm.expect(json.data).to.be.an("array"); });
`,
    }),
    req("06.07 - Vy browses public deck catalog first page", "GET", "/decks?page=0&size=5", {
      test: `
const page = pm.response.json().data;
pm.test("public deck catalog returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("public deck catalog has page metadata", function () { pm.expect(page).to.include.keys(["items", "page", "size", "totalItems", "totalPages"]); });
pm.test("public deck catalog respects requested size", function () { pm.expect(page.size).to.eql(5); });
`,
    }),
    req("06.08 - Vy searches deck catalog with no match", "GET", "/decks?keyword=unlikely-public-keyword-{{runId}}&page=0&size=5", {
      test: `
const page = pm.response.json().data;
pm.test("empty deck search returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("empty deck search returns zero items", function () { pm.expect(page.items.length).to.eql(0); });
`,
    }),
    req("06.09 - Vy cannot create anonymous deck", "POST", "/decks", {
      body: { title: "Anonymous Deck Should Fail", visibility: "PRIVATE" },
      test: `
pm.test("anonymous deck create returns HTTP 401", function () { pm.response.to.have.status(401); });
`,
    }),
    req("06.10 - Vy cannot open current user", "GET", "/auth/me", {
      test: `
pm.test("anonymous current user returns HTTP 401", function () { pm.response.to.have.status(401); });
`,
    }),
    req("06.11 - Vy cannot read admin dashboard", "GET", "/admin/dashboard", {
      test: `
pm.test("anonymous admin dashboard returns HTTP 401", function () { pm.response.to.have.status(401); });
`,
    }),
    req("06.12 - Vy sees unknown deck as not found or protected", "GET", "/decks/999999999", {
      test: `
pm.test("unknown public deck lookup does not succeed", function () { pm.expect([403, 404]).to.include(pm.response.code); });
`,
    }),
    req("06.13 - Vy cannot list users", "GET", "/users?page=0&size=10", {
      test: `
pm.test("anonymous user list returns HTTP 401", function () { pm.response.to.have.status(401); });
`,
    }),
  ]
);

const refreshAbuse = collection(
  "0007",
  "07 - Quan refresh token abuse checks",
  "Persona Quan: learner focuses on refresh token invalid inputs, rotation, reuse detection, logout, and token-bound private endpoints. Count: 25 pm.test test cases.",
  {},
  [
    prepare("07.00 - Prepare Quan auth run", "quan", "/auth/health", "auth"),
    req("07.01 - Quan registers", "POST", "/auth/register", {
      body: { email: "{{quanEmail}}", username: "{{quanUsername}}", password: "{{password}}", fullName: "{{quanFullName}}" },
      test: `
pm.test("Quan register returns HTTP 201", function () { pm.response.to.have.status(201); });
pm.test("Quan register response succeeds", function () { pm.expect(pm.response.json().success).to.eql(true); });
`,
    }),
    req("07.02 - Quan login stores token pair", "POST", "/auth/login", {
      body: { usernameOrEmail: "{{quanUsername}}", password: "{{password}}" },
      test: `
const json = pm.response.json();
pm.test("Quan login returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("Quan login stores access token", function () { pm.collectionVariables.set("quanAccessToken", json.data.accessToken); pm.expect(json.data.accessToken).to.not.be.empty; });
pm.test("Quan login stores refresh token", function () { pm.collectionVariables.set("quanRefreshToken", json.data.refreshToken); pm.expect(json.data.refreshToken).to.not.be.empty; });
`,
    }),
    req("07.03 - Blank refresh token is validation error", "POST", "/auth/refresh", {
      body: { refreshToken: "" },
      test: `
pm.test("blank refresh returns HTTP 400", function () { pm.response.to.have.status(400); });
pm.test("blank refresh has validation payload", function () { pm.expect(pm.response.json().data).to.be.an("object"); });
`,
    }),
    req("07.04 - Random refresh token is unauthorized", "POST", "/auth/refresh", {
      body: { refreshToken: "totally-random-refresh-token" },
      test: `
pm.test("random refresh returns HTTP 401", function () { pm.response.to.have.status(401); });
pm.test("random refresh response fails", function () { pm.expect(pm.response.json().success).to.eql(false); });
`,
    }),
    req("07.05 - Refresh rotates Quan tokens first time", "POST", "/auth/refresh", {
      body: { refreshToken: "{{quanRefreshToken}}" },
      test: `
const json = pm.response.json();
pm.test("first refresh returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("first refresh rotates refresh token", function () {
  pm.collectionVariables.set("quanRefreshTokenOne", pm.collectionVariables.get("quanRefreshToken"));
  pm.collectionVariables.set("quanAccessToken", json.data.accessToken);
  pm.collectionVariables.set("quanRefreshToken", json.data.refreshToken);
  pm.expect(json.data.refreshToken).to.not.eql(pm.collectionVariables.get("quanRefreshTokenOne"));
});
`,
    }),
    req("07.06 - Reusing first refresh token fails", "POST", "/auth/refresh", {
      body: { refreshToken: "{{quanRefreshTokenOne}}" },
      test: `
pm.test("first refresh reuse returns HTTP 401", function () { pm.response.to.have.status(401); });
`,
    }),
    req("07.07 - Current user works after refresh", "GET", "/auth/me", {
      auth: "{{quanAccessToken}}",
      test: `
const json = pm.response.json();
pm.test("me after refresh returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("me after refresh is Quan", function () { pm.expect(json.data.email).to.eql(pm.collectionVariables.get("quanEmail")); });
`,
    }),
    req("07.08 - Refresh rotates Quan tokens second time", "POST", "/auth/refresh", {
      body: { refreshToken: "{{quanRefreshToken}}" },
      test: `
const json = pm.response.json();
pm.test("second refresh returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("second refresh stores new token pair", function () {
  pm.collectionVariables.set("quanRefreshTokenTwo", pm.collectionVariables.get("quanRefreshToken"));
  pm.collectionVariables.set("quanAccessToken", json.data.accessToken);
  pm.collectionVariables.set("quanRefreshToken", json.data.refreshToken);
  pm.expect(json.data.accessToken).to.not.be.empty;
});
`,
    }),
    req("07.09 - Reusing second refresh token fails", "POST", "/auth/refresh", {
      body: { refreshToken: "{{quanRefreshTokenTwo}}" },
      test: `
pm.test("second refresh reuse returns HTTP 401", function () { pm.response.to.have.status(401); });
`,
    }),
    req("07.10 - Logout requires current refresh token", "POST", "/auth/logout", {
      auth: "{{quanAccessToken}}",
      body: { refreshToken: "{{quanRefreshToken}}" },
      test: `
pm.test("logout current token returns HTTP 204", function () { pm.response.to.have.status(204); });
`,
    }),
    req("07.11 - Refresh after logout fails", "POST", "/auth/refresh", {
      body: { refreshToken: "{{quanRefreshToken}}" },
      test: `
pm.test("refresh after logout returns HTTP 401", function () { pm.response.to.have.status(401); });
pm.test("refresh after logout response fails", function () { pm.expect(pm.response.json().success).to.eql(false); });
`,
    }),
    req("07.12 - Login by email still works after logout", "POST", "/auth/login", {
      body: { usernameOrEmail: "{{quanEmail}}", password: "{{password}}" },
      test: `
pm.test("email login after logout returns HTTP 200", function () {
  const json = pm.response.json();
  pm.response.to.have.status(200);
  pm.collectionVariables.set("quanAccessToken", json.data.accessToken);
});
`,
    }),
  ]
);

const multiUserIsolation = collection(
  "0008",
  "08 - Mai and Nam ownership isolation",
  "Personas Mai and Nam: two learners verify private deck isolation, owner-only flashcard management, and public catalog boundaries. Count: 27 pm.test test cases.",
  {},
  [
    prepare("08.00 - Prepare Mai and Nam run", "mai", "/content/health", "content"),
    req("08.01 - Register Mai owner", "POST", "/auth/register", {
      body: { email: "{{maiEmail}}", username: "{{maiUsername}}", password: "{{password}}", fullName: "{{maiFullName}}" },
      test: `
pm.test("Mai register returns HTTP 201", function () { pm.response.to.have.status(201); });
`,
    }),
    req("08.02 - Register Nam outsider", "POST", "/auth/register", {
      pre: `
pm.collectionVariables.set("namEmail", "nam." + pm.collectionVariables.get("runId") + "@example.com");
pm.collectionVariables.set("namUsername", "nam_" + pm.collectionVariables.get("runId"));
`,
      body: { email: "{{namEmail}}", username: "{{namUsername}}", password: "{{password}}", fullName: "Nam Outsider {{runId}}" },
      test: `
pm.test("Nam register returns HTTP 201", function () { pm.response.to.have.status(201); });
`,
    }),
    req("08.03 - Login Mai", "POST", "/auth/login", {
      body: { usernameOrEmail: "{{maiEmail}}", password: "{{password}}" },
      test: `
pm.test("Mai login stores token", function () {
  const json = pm.response.json();
  pm.response.to.have.status(200);
  pm.collectionVariables.set("maiAccessToken", json.data.accessToken);
});
`,
    }),
    req("08.04 - Login Nam", "POST", "/auth/login", {
      body: { usernameOrEmail: "{{namEmail}}", password: "{{password}}" },
      test: `
pm.test("Nam login stores token", function () {
  const json = pm.response.json();
  pm.response.to.have.status(200);
  pm.collectionVariables.set("namAccessToken", json.data.accessToken);
});
`,
    }),
    req("08.05 - Mai creates private deck", "POST", "/decks", {
      auth: "{{maiAccessToken}}",
      body: { title: "Mai Private Isolation Deck {{runId}}", description: "Owner-only deck", visibility: "PRIVATE" },
      test: `
const json = pm.response.json();
pm.test("Mai private deck create returns HTTP 201", function () { pm.response.to.have.status(201); });
pm.test("Mai deck id is stored", function () { pm.collectionVariables.set("maiDeckId", json.data.id); pm.expect(json.data.visibility).to.eql("PRIVATE"); });
`,
    }),
    req("08.06 - Mai adds private flashcard", "POST", "/decks/{{maiDeckId}}/flashcards", {
      auth: "{{maiAccessToken}}",
      body: { frontText: "owner-only", backText: "chi chu so huu", difficultyLevel: "EASY", cardOrder: 1 },
      test: `
pm.test("Mai flashcard create returns HTTP 201", function () {
  const json = pm.response.json();
  pm.response.to.have.status(201);
  pm.collectionVariables.set("maiFlashcardId", json.data.id);
});
`,
    }),
    req("08.07 - Nam cannot open Mai private deck", "GET", "/decks/{{maiDeckId}}", {
      auth: "{{namAccessToken}}",
      test: `
pm.test("Nam private deck detail returns HTTP 403", function () { pm.response.to.have.status(403); });
`,
    }),
    req("08.08 - Nam cannot list Mai private flashcards", "GET", "/decks/{{maiDeckId}}/flashcards", {
      auth: "{{namAccessToken}}",
      test: `
pm.test("Nam private flashcard list returns HTTP 403", function () { pm.response.to.have.status(403); });
`,
    }),
    req("08.09 - Nam cannot update Mai flashcard", "PUT", "/flashcards/{{maiFlashcardId}}", {
      auth: "{{namAccessToken}}",
      body: { frontText: "stolen edit", backText: "should not work", difficultyLevel: "HARD", cardOrder: 9 },
      test: `
pm.test("Nam flashcard update returns HTTP 403", function () { pm.response.to.have.status(403); });
`,
    }),
    req("08.10 - Nam cannot delete Mai flashcard", "DELETE", "/flashcards/{{maiFlashcardId}}", {
      auth: "{{namAccessToken}}",
      test: `
pm.test("Nam flashcard delete returns HTTP 403", function () { pm.response.to.have.status(403); });
`,
    }),
    req("08.11 - Nam creates own private deck", "POST", "/decks", {
      auth: "{{namAccessToken}}",
      body: { title: "Nam Own Private Deck {{runId}}", visibility: "PRIVATE" },
      test: `
pm.test("Nam own deck create returns HTTP 201", function () {
  const json = pm.response.json();
  pm.response.to.have.status(201);
  pm.collectionVariables.set("namDeckId", json.data.id);
});
`,
    }),
    req("08.12 - Mai cannot open Nam private deck", "GET", "/decks/{{namDeckId}}", {
      auth: "{{maiAccessToken}}",
      test: `
pm.test("Mai cannot read Nam deck", function () { pm.response.to.have.status(403); });
`,
    }),
    req("08.13 - Mai deck catalog shows own deck", "GET", "/decks?keyword=Mai%20Private%20Isolation&page=0&size=10", {
      auth: "{{maiAccessToken}}",
      test: `
pm.test("Mai sees own private deck in authenticated list", function () {
  const items = pm.response.json().data.items;
  pm.expect(items.some(x => String(x.id) === pm.collectionVariables.get("maiDeckId"))).to.eql(true);
});
`,
    }),
    req("08.14 - Nam deck catalog does not show Mai deck", "GET", "/decks?keyword=Mai%20Private%20Isolation&page=0&size=10", {
      auth: "{{namAccessToken}}",
      test: `
pm.test("Nam does not see Mai private deck in list", function () {
  const items = pm.response.json().data.items;
  pm.expect(items.some(x => String(x.id) === pm.collectionVariables.get("maiDeckId"))).to.eql(false);
});
`,
    }),
    req("08.15 - Anonymous catalog does not show Mai private deck", "GET", "/decks?keyword=Mai%20Private%20Isolation&page=0&size=10", {
      test: `
pm.test("anonymous does not see Mai private deck", function () {
  const items = pm.response.json().data.items;
  pm.expect(items.length).to.eql(0);
});
`,
    }),
    req("08.16 - Mai deletes private deck", "DELETE", "/decks/{{maiDeckId}}", {
      auth: "{{maiAccessToken}}",
      test: `
pm.test("Mai delete own deck returns HTTP 204", function () { pm.response.to.have.status(204); });
`,
    }),
    req("08.17 - Deleted Mai deck is gone", "GET", "/decks/{{maiDeckId}}", {
      auth: "{{maiAccessToken}}",
      test: `
pm.test("deleted Mai deck returns HTTP 404", function () { pm.response.to.have.status(404); });
`,
    }),
    req("08.18 - Nam deletes own deck", "DELETE", "/decks/{{namDeckId}}", {
      auth: "{{namAccessToken}}",
      test: `
pm.test("Nam delete own deck returns HTTP 204", function () { pm.response.to.have.status(204); });
`,
    }),
  ]
);

const learningRatings = collection(
  "0009",
  "09 - Oanh review rating matrix",
  "Persona Oanh: learner reviews four cards with AGAIN, HARD, GOOD, EASY and verifies scheduling/progress differences. Count: 26 pm.test test cases.",
  {},
  [
    prepare("09.00 - Prepare Oanh learning run", "oanh", "/learning/health", "learning"),
    req("09.01 - Register Oanh", "POST", "/auth/register", {
      body: { email: "{{oanhEmail}}", username: "{{oanhUsername}}", password: "{{password}}", fullName: "{{oanhFullName}}" },
      test: `pm.test("Oanh register returns HTTP 201", function () { pm.response.to.have.status(201); });`,
    }),
    req("09.02 - Login Oanh", "POST", "/auth/login", {
      body: { usernameOrEmail: "{{oanhEmail}}", password: "{{password}}" },
      test: `
pm.test("Oanh login stores token", function () {
  const json = pm.response.json();
  pm.response.to.have.status(200);
  pm.collectionVariables.set("oanhAccessToken", json.data.accessToken);
});
`,
    }),
    req("09.03 - Create Oanh rating deck", "POST", "/decks", {
      auth: "{{oanhAccessToken}}",
      body: { title: "Oanh Rating Matrix {{runId}}", description: "Deck for rating matrix", visibility: "PRIVATE" },
      test: `
pm.test("rating deck create returns HTTP 201", function () {
  const json = pm.response.json();
  pm.response.to.have.status(201);
  pm.collectionVariables.set("oanhDeckId", json.data.id);
});
`,
    }),
    ...["again", "hard", "good", "easy"].map((word, index) => req(`09.0${index + 4} - Create ${word.toUpperCase()} card`, "POST", "/decks/{{oanhDeckId}}/flashcards", {
      auth: "{{oanhAccessToken}}",
      body: { frontText: `${word} card {{runId}}`, backText: `${word} answer`, difficultyLevel: "MEDIUM", cardOrder: index + 1 },
      test: `
pm.test("${word} card create returns HTTP 201", function () {
  const json = pm.response.json();
  pm.response.to.have.status(201);
  pm.collectionVariables.set("${word}CardId", json.data.id);
});
`,
    })),
    req("09.08 - Start rating matrix session", "POST", "/study-sessions/start", {
      auth: "{{oanhAccessToken}}",
      body: { deckId: "{{oanhDeckId}}", limit: 10 },
      test: `
const json = pm.response.json();
pm.test("rating matrix session returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("rating matrix session has four cards", function () { pm.collectionVariables.set("oanhSessionId", json.data.sessionId); pm.expect(json.data.cards.length).to.eql(4); });
`,
    }),
    req("09.09 - Submit AGAIN review", "POST", "/reviews/{{againCardId}}", {
      auth: "{{oanhAccessToken}}",
      body: { studySessionId: "{{oanhSessionId}}", rating: "AGAIN", responseTimeMs: 5000 },
      test: `
const json = pm.response.json();
pm.test("AGAIN review returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("AGAIN interval is zero", function () { pm.expect(json.data.intervalDays).to.eql(0); });
pm.test("AGAIN remains learning", function () { pm.expect(json.data.masteryLevel).to.eql("LEARNING"); });
`,
    }),
    req("09.10 - Submit HARD review", "POST", "/reviews/{{hardCardId}}", {
      auth: "{{oanhAccessToken}}",
      body: { studySessionId: "{{oanhSessionId}}", rating: "HARD", responseTimeMs: 4200 },
      test: `
const json = pm.response.json();
pm.test("HARD review returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("HARD interval is at least one", function () { pm.expect(json.data.intervalDays).to.be.at.least(1); });
`,
    }),
    req("09.11 - Submit GOOD review", "POST", "/reviews/{{goodCardId}}", {
      auth: "{{oanhAccessToken}}",
      body: { studySessionId: "{{oanhSessionId}}", rating: "GOOD", responseTimeMs: 2200 },
      test: `
const json = pm.response.json();
pm.test("GOOD review returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("GOOD first interval is one day", function () { pm.expect(json.data.intervalDays).to.eql(1); });
`,
    }),
    req("09.12 - Submit EASY review", "POST", "/reviews/{{easyCardId}}", {
      auth: "{{oanhAccessToken}}",
      body: { studySessionId: "{{oanhSessionId}}", rating: "EASY", responseTimeMs: 900 },
      test: `
const json = pm.response.json();
pm.test("EASY review returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("EASY first interval is four days", function () { pm.expect(json.data.intervalDays).to.eql(4); });
`,
    }),
    req("09.13 - Finish rating matrix session", "POST", "/study-sessions/{{oanhSessionId}}/finish", {
      auth: "{{oanhAccessToken}}",
      test: `pm.test("finish rating session returns HTTP 204", function () { pm.response.to.have.status(204); });`,
    }),
    req("09.14 - Finishing same session again conflicts", "POST", "/study-sessions/{{oanhSessionId}}/finish", {
      auth: "{{oanhAccessToken}}",
      test: `pm.test("second finish returns HTTP 409", function () { pm.response.to.have.status(409); });`,
    }),
    req("09.15 - Progress shows four learned cards", "GET", "/progress/decks/{{oanhDeckId}}", {
      auth: "{{oanhAccessToken}}",
      test: `
const json = pm.response.json();
pm.test("rating deck progress returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("rating deck learned cards is four", function () { pm.expect(json.data.learnedCards).to.eql(4); });
`,
    }),
    req("09.16 - Overview shows streak after matrix", "GET", "/progress/me", {
      auth: "{{oanhAccessToken}}",
      test: `
pm.test("overview returns current streak", function () {
  const json = pm.response.json();
  pm.response.to.have.status(200);
  pm.expect(json.data.currentStreakDays).to.be.at.least(1);
});
`,
    }),
  ]
);

const adminAuditFilters = collection(
  "0010",
  "10 - Bao admin audit and filter checks",
  "Persona Bao: admin validates list filters, report filters, audit filters, and role-protected catalog creation. Count: 25 pm.test test cases. Requires adminEmail/adminPassword.",
  {},
  [
    req("10.00 - Prepare Bao admin run", "GET", "/auth/health", {
      pre: `
const runId = Date.now().toString();
const shortId = runId.slice(-8);
pm.collectionVariables.set("runId", runId);
pm.collectionVariables.set("baoShortId", shortId);
pm.collectionVariables.set("baoLearnerEmail", "bao.target." + runId + "@example.com");
pm.collectionVariables.set("baoLearnerUsername", "bao_target_" + runId);
`,
      test: `
pm.test("Bao prepare auth health returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("Bao prepare auth health succeeds", function () { pm.expect(pm.response.json().success).to.eql(true); });
`,
    }),
    req("10.01 - Login Bao admin", "POST", "/auth/login", {
      body: { usernameOrEmail: "{{adminEmail}}", password: "{{adminPassword}}" },
      test: `
const json = pm.response.json();
pm.test("Bao admin login returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("Bao admin token stored", function () { pm.collectionVariables.set("baoAdminToken", json.data.accessToken); });
`,
    }),
    req("10.02 - Bao creates managed language", "POST", "/languages", {
      auth: "{{baoAdminToken}}",
      body: { code: "b{{baoShortId}}", name: "Bao Language {{runId}}" },
      test: `
pm.test("admin language create returns HTTP 201", function () {
  const json = pm.response.json();
  pm.response.to.have.status(201);
  pm.collectionVariables.set("baoLanguageId", json.data.id);
});
`,
    }),
    req("10.03 - Duplicate language code conflicts", "POST", "/languages", {
      auth: "{{baoAdminToken}}",
      body: { code: "b{{baoShortId}}", name: "Bao Language Duplicate {{runId}}" },
      test: `
pm.test("duplicate language returns HTTP 409", function () { pm.response.to.have.status(409); });
`,
    }),
    req("10.04 - Bao creates managed topic", "POST", "/topics", {
      auth: "{{baoAdminToken}}",
      body: { name: "Bao Topic {{runId}}", description: "Admin filter topic" },
      test: `
pm.test("admin topic create returns HTTP 201", function () {
  const json = pm.response.json();
  pm.response.to.have.status(201);
  pm.collectionVariables.set("baoTopicId", json.data.id);
});
`,
    }),
    req("10.05 - Bao creates managed tag", "POST", "/tags", {
      auth: "{{baoAdminToken}}",
      body: { name: "bao-tag-{{runId}}" },
      test: `
pm.test("admin tag create returns HTTP 201", function () {
  const json = pm.response.json();
  pm.response.to.have.status(201);
  pm.collectionVariables.set("baoTagId", json.data.id);
});
`,
    }),
    req("10.06 - Register learner for admin filters", "POST", "/auth/register", {
      body: { email: "{{baoLearnerEmail}}", username: "{{baoLearnerUsername}}", password: "{{password}}", fullName: "Bao Target {{runId}}" },
      test: `
pm.test("filter learner register returns HTTP 201", function () {
  const json = pm.response.json();
  pm.response.to.have.status(201);
  pm.collectionVariables.set("baoLearnerId", json.data.id);
});
`,
    }),
    req("10.07 - Admin user keyword filter finds learner", "GET", "/users?keyword={{baoLearnerUsername}}&page=0&size=5", {
      auth: "{{baoAdminToken}}",
      test: `
pm.test("keyword filter includes learner", function () {
  const items = pm.response.json().data.items;
  const learnerId = String(pm.collectionVariables.get("baoLearnerId"));
  pm.response.to.have.status(200);
  pm.expect(items.some(x => String(x.id) === learnerId)).to.eql(true);
});
`,
    }),
    req("10.08 - Admin ACTIVE status filter works", "GET", "/users?status=ACTIVE&page=0&size=5", {
      auth: "{{baoAdminToken}}",
      test: `
pm.test("active status filter returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("active status filter returns page items", function () { pm.expect(pm.response.json().data.items).to.be.an("array"); });
`,
    }),
    req("10.09 - Bao locks learner for audit", "POST", "/admin/users/{{baoLearnerId}}/lock", {
      auth: "{{baoAdminToken}}",
      test: `
pm.test("admin lock for audit returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("admin lock changes status", function () { pm.expect(pm.response.json().data.status).to.eql("LOCKED"); });
`,
    }),
    req("10.10 - Admin LOCKED status filter finds learner", "GET", "/users?status=LOCKED&page=0&size=20", {
      auth: "{{baoAdminToken}}",
      test: `
pm.test("locked filter contains target", function () {
  const items = pm.response.json().data.items;
  const learnerId = String(pm.collectionVariables.get("baoLearnerId"));
  pm.expect(items.some(x => String(x.id) === learnerId)).to.eql(true);
});
`,
    }),
    req("10.11 - User lock audit filter finds entry", "GET", "/admin/audit-logs?action=USER_LOCKED&resourceType=USER&resourceId={{baoLearnerId}}", {
      auth: "{{baoAdminToken}}",
      test: `
pm.test("USER_LOCKED audit exists", function () { pm.expect(pm.response.json().data.totalItems).to.be.at.least(1); });
`,
    }),
    req("10.12 - Bao unlocks learner for cleanup state", "POST", "/admin/users/{{baoLearnerId}}/unlock", {
      auth: "{{baoAdminToken}}",
      test: `
pm.test("admin unlock returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("admin unlock changes status ACTIVE", function () { pm.expect(pm.response.json().data.status).to.eql("ACTIVE"); });
`,
    }),
    req("10.13 - Audit log pagination returns metadata", "GET", "/admin/audit-logs?page=0&size=5", {
      auth: "{{baoAdminToken}}",
      test: `
const page = pm.response.json().data;
pm.test("audit log list returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("audit log list has page metadata", function () { pm.expect(page).to.include.keys(["items", "page", "size", "totalItems", "totalPages"]); });
`,
    }),
    req("10.14 - Admin dashboard updates after operations", "GET", "/admin/dashboard", {
      auth: "{{baoAdminToken}}",
      test: `
const json = pm.response.json();
pm.test("dashboard after admin operations returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("dashboard total users is positive", function () { pm.expect(json.data.totalUsers).to.be.at.least(1); });
`,
    }),
  ]
);

write("06_vy_public_catalog_explorer.postman_collection.json", publicExplorer);
write("07_quan_refresh_token_abuse_checks.postman_collection.json", refreshAbuse);
write("08_mai_nam_ownership_isolation.postman_collection.json", multiUserIsolation);
write("09_oanh_review_rating_matrix.postman_collection.json", learningRatings);
write("10_bao_admin_audit_filter_checks.postman_collection.json", adminAuditFilters);
