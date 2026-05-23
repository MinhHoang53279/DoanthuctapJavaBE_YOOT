const fs = require("fs");
const path = require("path");

const SCHEMA = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json";
const outDir = __dirname;

function uid(seed) {
  return `8f8c${seed}-4f7d-4e9e-9d1a-${seed}${seed}${seed}${seed}`.slice(0, 36);
}

function rawBody(value) {
  return {
    mode: "raw",
    raw: JSON.stringify(value, null, 2),
    options: {
      raw: {
        language: "json",
      },
    },
  };
}

function request(name, method, urlPath, options = {}) {
  const headers = options.headers || [];
  if (options.body) {
    headers.push({ key: "Content-Type", value: "application/json" });
  }
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
    item.request.body = rawBody(options.body);
  }
  const events = [];
  if (options.pre) {
    events.push({
      listen: "prerequest",
      script: { type: "text/javascript", exec: asLines(options.pre) },
    });
  }
  if (options.test) {
    events.push({
      listen: "test",
      script: { type: "text/javascript", exec: asLines(options.test) },
    });
  }
  if (events.length) {
    item.event = events;
  }
  return item;
}

function asLines(script) {
  return script.trim().split(/\r?\n/);
}

function vars(values) {
  return Object.entries(values).map(([key, value]) => ({ key, value }));
}

function collection(idSeed, name, description, variableValues, items) {
  return {
    info: {
      _postman_id: uid(idSeed),
      name,
      description,
      schema: SCHEMA,
    },
    variable: vars({
      baseUrl: "http://localhost:8080/api/v1",
      password: "Password@123",
      ...variableValues,
    }),
    item: items,
  };
}

function writeCollection(fileName, data) {
  const filePath = path.join(outDir, fileName);
  fs.writeFileSync(filePath, JSON.stringify(data, null, 2) + "\n", "utf8");
  const count = JSON.stringify(data).match(/pm\.test\(/g)?.length || 0;
  console.log(`${fileName}: ${count} test cases`);
}

function preparePersona(personaPrefix, healthPath, moduleName) {
  return request(`00 - Prepare ${capitalize(personaPrefix)} fresh persona run`, "GET", "/auth/health", {
    pre: `
const runId = Date.now().toString();
pm.collectionVariables.set("runId", runId);
pm.collectionVariables.set("runIdShort", runId.slice(-8));
pm.collectionVariables.set("${personaPrefix}Email", "${personaPrefix}." + runId + "@example.com");
pm.collectionVariables.set("${personaPrefix}Username", "${personaPrefix}_" + runId);
pm.collectionVariables.set("${personaPrefix}FullName", "${capitalize(personaPrefix)} " + runId);
`,
    test: `
const json = pm.response.json();
pm.test("prepare returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("prepare confirms public auth health", function () { pm.expect(json.data.module).to.eql("auth"); });
`,
  });
}

function capitalize(value) {
  return value.charAt(0).toUpperCase() + value.slice(1);
}

const learnerAuth = collection(
  "0001",
  "01 - Minh learner auth lifecycle",
  "Persona Minh: a new learner signs up, logs in, rotates refresh token, checks current user, and logs out. Count: 27 Postman pm.test test cases.",
  {},
  [
    preparePersona("minh", "/auth/health", "auth"),
    request("01 - Register Minh as learner", "POST", "/auth/register", {
      body: {
        email: "{{minhEmail}}",
        username: "{{minhUsername}}",
        password: "{{password}}",
        fullName: "{{minhFullName}}",
      },
      test: `
const json = pm.response.json();
pm.test("register creates learner with HTTP 201", function () { pm.response.to.have.status(201); });
pm.test("register response is successful", function () { pm.expect(json.success).to.eql(true); });
pm.test("register stores Minh user id and LEARNER role", function () {
  pm.collectionVariables.set("minhUserId", json.data.id);
  pm.expect(json.data.roles).to.include("LEARNER");
});
`,
    }),
    request("02 - Duplicate email is rejected", "POST", "/auth/register", {
      body: {
        email: "{{minhEmail}}",
        username: "copy_{{minhUsername}}",
        password: "{{password}}",
        fullName: "Copy Minh",
      },
      test: `
const json = pm.response.json();
pm.test("duplicate email returns HTTP 409", function () { pm.response.to.have.status(409); });
pm.test("duplicate email response is unsuccessful", function () { pm.expect(json.success).to.eql(false); });
`,
    }),
    request("03 - Invalid email validation", "POST", "/auth/register", {
      body: {
        email: "not-an-email",
        username: "bad_{{minhUsername}}",
        password: "{{password}}",
        fullName: "Bad Email",
      },
      test: `
const json = pm.response.json();
pm.test("invalid email returns HTTP 400", function () { pm.response.to.have.status(400); });
pm.test("invalid email has validation payload", function () { pm.expect(json.data).to.be.an("object"); });
`,
    }),
    request("04 - Login Minh by email", "POST", "/auth/login", {
      body: {
        usernameOrEmail: "{{minhEmail}}",
        password: "{{password}}",
      },
      test: `
const json = pm.response.json();
pm.test("login returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("login returns bearer tokens", function () {
  pm.collectionVariables.set("minhAccessToken", json.data.accessToken);
  pm.collectionVariables.set("minhRefreshToken", json.data.refreshToken);
  pm.expect(json.data.tokenType).to.eql("Bearer");
});
pm.test("login access token is not blank", function () { pm.expect(json.data.accessToken).to.not.be.empty; });
`,
    }),
    request("05 - Current user without token is blocked", "GET", "/auth/me", {
      test: `
const json = pm.response.json();
pm.test("current user without token returns HTTP 401", function () { pm.response.to.have.status(401); });
pm.test("current user without token is unsuccessful", function () { pm.expect(json.success).to.eql(false); });
`,
    }),
    request("06 - Current user with Minh token", "GET", "/auth/me", {
      auth: "{{minhAccessToken}}",
      test: `
const json = pm.response.json();
pm.test("current user returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("current user matches Minh email", function () { pm.expect(json.data.email).to.eql(pm.collectionVariables.get("minhEmail")); });
pm.test("current user includes LEARNER role", function () { pm.expect(json.data.roles).to.include("LEARNER"); });
`,
    }),
    request("07 - Refresh token rotates credentials", "POST", "/auth/refresh", {
      body: {
        refreshToken: "{{minhRefreshToken}}",
      },
      test: `
const json = pm.response.json();
pm.test("refresh returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("refresh issues a new access token", function () { pm.expect(json.data.accessToken).to.not.be.empty; });
pm.test("refresh rotates refresh token", function () {
  pm.collectionVariables.set("minhOldRefreshToken", pm.collectionVariables.get("minhRefreshToken"));
  pm.expect(json.data.refreshToken).to.not.eql(pm.collectionVariables.get("minhRefreshToken"));
  pm.collectionVariables.set("minhAccessToken", json.data.accessToken);
  pm.collectionVariables.set("minhRefreshToken", json.data.refreshToken);
});
`,
    }),
    request("08 - Old refresh token cannot be reused", "POST", "/auth/refresh", {
      body: {
        refreshToken: "{{minhOldRefreshToken}}",
      },
      test: `
const json = pm.response.json();
pm.test("old refresh returns HTTP 401", function () { pm.response.to.have.status(401); });
pm.test("old refresh is unsuccessful", function () { pm.expect(json.success).to.eql(false); });
`,
    }),
    request("09 - Logout Minh", "POST", "/auth/logout", {
      auth: "{{minhAccessToken}}",
      body: {
        refreshToken: "{{minhRefreshToken}}",
      },
      test: `
pm.test("logout returns HTTP 204", function () { pm.response.to.have.status(204); });
`,
    }),
    request("10 - Refresh after logout is rejected", "POST", "/auth/refresh", {
      body: {
        refreshToken: "{{minhRefreshToken}}",
      },
      test: `
const json = pm.response.json();
pm.test("logged out refresh returns HTTP 401", function () { pm.response.to.have.status(401); });
pm.test("logged out refresh is unsuccessful", function () { pm.expect(json.success).to.eql(false); });
`,
    }),
    request("11 - Wrong password login is rejected", "POST", "/auth/login", {
      body: {
        usernameOrEmail: "{{minhEmail}}",
        password: "WrongPassword@123",
      },
      test: `
const json = pm.response.json();
pm.test("wrong password returns HTTP 401", function () { pm.response.to.have.status(401); });
pm.test("wrong password response is unsuccessful", function () { pm.expect(json.success).to.eql(false); });
`,
    }),
  ]
);

const learnerStudy = collection(
  "0002",
  "02 - Han learner private deck and learning",
  "Persona Han: a learner creates a private deck, adds flashcards, studies, reviews, checks progress, then cleans up. Count: 27 Postman pm.test test cases.",
  {},
  [
    preparePersona("han", "/content/health", "content"),
    request("01 - Register Han learner", "POST", "/auth/register", {
      body: { email: "{{hanEmail}}", username: "{{hanUsername}}", password: "{{password}}", fullName: "{{hanFullName}}" },
      test: `
pm.test("Han register returns HTTP 201", function () { pm.response.to.have.status(201); });
`,
    }),
    request("02 - Login Han learner", "POST", "/auth/login", {
      body: { usernameOrEmail: "{{hanEmail}}", password: "{{password}}" },
      test: `
const json = pm.response.json();
pm.test("Han login returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("Han login stores access and refresh tokens", function () {
  pm.collectionVariables.set("hanAccessToken", json.data.accessToken);
  pm.collectionVariables.set("hanRefreshToken", json.data.refreshToken);
  pm.expect(json.data.refreshToken).to.not.be.empty;
});
`,
    }),
    request("03 - Load language catalog", "GET", "/languages", {
      test: `
const items = pm.response.json().data;
pm.test("language catalog returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("language catalog has English and Vietnamese", function () {
  pm.collectionVariables.set("englishId", items.find(x => x.code === "en").id);
  pm.collectionVariables.set("vietnameseId", items.find(x => x.code === "vi").id);
  pm.expect(items.map(x => x.code)).to.include.members(["en", "vi"]);
});
`,
    }),
    request("04 - Load topic catalog", "GET", "/topics", {
      test: `
const items = pm.response.json().data;
pm.test("topic catalog has Daily Life", function () {
  pm.collectionVariables.set("dailyLifeTopicId", items.find(x => x.name === "Daily Life").id);
  pm.expect(items.map(x => x.name)).to.include("Daily Life");
});
`,
    }),
    request("05 - Create Han private deck", "POST", "/decks", {
      auth: "{{hanAccessToken}}",
      body: {
        title: "Han Private Daily Vocabulary {{runId}}",
        description: "Private deck for daily study automation",
        sourceLanguageId: "{{vietnameseId}}",
        targetLanguageId: "{{englishId}}",
        topicId: "{{dailyLifeTopicId}}",
        visibility: "PRIVATE",
      },
      test: `
const json = pm.response.json();
pm.test("private deck create returns HTTP 201", function () { pm.response.to.have.status(201); });
pm.test("private deck is DRAFT and PRIVATE", function () {
  pm.collectionVariables.set("hanDeckId", json.data.id);
  pm.expect(json.data.visibility).to.eql("PRIVATE");
  pm.expect(json.data.status).to.eql("DRAFT");
});
`,
    }),
    request("06 - Anonymous cannot open Han private deck", "GET", "/decks/{{hanDeckId}}", {
      test: `
pm.test("anonymous private deck detail returns HTTP 403", function () { pm.response.to.have.status(403); });
`,
    }),
    request("07 - Han opens own private deck", "GET", "/decks/{{hanDeckId}}", {
      auth: "{{hanAccessToken}}",
      test: `
pm.test("owner private deck detail returns HTTP 200", function () { pm.response.to.have.status(200); });
`,
    }),
    request("08 - Han updates deck title", "PUT", "/decks/{{hanDeckId}}", {
      auth: "{{hanAccessToken}}",
      body: {
        title: "Han Updated Private Vocabulary {{runId}}",
        description: "Updated by Han",
        sourceLanguageId: "{{vietnameseId}}",
        targetLanguageId: "{{englishId}}",
        topicId: "{{dailyLifeTopicId}}",
        visibility: "PRIVATE",
      },
      test: `
pm.test("deck update returns HTTP 200", function () { pm.response.to.have.status(200); });
`,
    }),
    request("09 - Add apple flashcard", "POST", "/decks/{{hanDeckId}}/flashcards", {
      auth: "{{hanAccessToken}}",
      body: {
        frontText: "apple",
        backText: "qua tao",
        pronunciation: "/ap-uhl/",
        exampleSentence: "I eat an apple every day.",
        note: "noun",
        difficultyLevel: "EASY",
        cardOrder: 1,
      },
      test: `
const json = pm.response.json();
pm.test("apple flashcard create returns HTTP 201", function () { pm.response.to.have.status(201); });
pm.test("apple flashcard id is stored", function () {
  pm.collectionVariables.set("hanFlashcardOneId", json.data.id);
  pm.expect(json.data.frontText).to.eql("apple");
});
`,
    }),
    request("10 - Add book flashcard", "POST", "/decks/{{hanDeckId}}/flashcards", {
      auth: "{{hanAccessToken}}",
      body: { frontText: "book", backText: "quyen sach", difficultyLevel: "MEDIUM", cardOrder: 2 },
      test: `
const json = pm.response.json();
pm.test("book flashcard create returns HTTP 201", function () {
  pm.response.to.have.status(201);
  pm.collectionVariables.set("hanFlashcardTwoId", json.data.id);
});
`,
    }),
    request("11 - List Han flashcards", "GET", "/decks/{{hanDeckId}}/flashcards?page=0&size=10", {
      auth: "{{hanAccessToken}}",
      test: `
pm.test("owner flashcard list returns at least two cards", function () {
  const json = pm.response.json();
  pm.response.to.have.status(200);
  pm.expect(json.data.totalItems).to.be.at.least(2);
});
`,
    }),
    request("12 - Start Han study session", "POST", "/study-sessions/start", {
      auth: "{{hanAccessToken}}",
      body: { deckId: "{{hanDeckId}}", limit: 20 },
      test: `
const json = pm.response.json();
pm.test("study session starts with HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("study session returns cards", function () {
  pm.collectionVariables.set("hanSessionId", json.data.sessionId);
  pm.collectionVariables.set("hanStudyFlashcardId", json.data.cards[0].flashcardId);
  pm.expect(json.data.cards.length).to.be.at.least(1);
});
`,
    }),
    request("13 - Submit GOOD review", "POST", "/reviews/{{hanStudyFlashcardId}}", {
      auth: "{{hanAccessToken}}",
      body: { studySessionId: "{{hanSessionId}}", rating: "GOOD", responseTimeMs: 1400 },
      test: `
const json = pm.response.json();
pm.test("GOOD review returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("GOOD review moves card into learning", function () { pm.expect(json.data.masteryLevel).to.eql("LEARNING"); });
`,
    }),
    request("14 - Reviews today works after review", "GET", "/reviews/today?limit=20", {
      auth: "{{hanAccessToken}}",
      test: `
pm.test("reviews today returns HTTP 200", function () { pm.response.to.have.status(200); });
`,
    }),
    request("15 - Finish Han study session", "POST", "/study-sessions/{{hanSessionId}}/finish", {
      auth: "{{hanAccessToken}}",
      test: `
pm.test("finish study session returns HTTP 204", function () { pm.response.to.have.status(204); });
`,
    }),
    request("16 - Han deck progress is updated", "GET", "/progress/decks/{{hanDeckId}}", {
      auth: "{{hanAccessToken}}",
      test: `
pm.test("deck progress returns learned card count", function () {
  const json = pm.response.json();
  pm.response.to.have.status(200);
  pm.expect(json.data.learnedCards).to.be.at.least(1);
});
`,
    }),
    request("17 - Han overview includes streak", "GET", "/progress/me", {
      auth: "{{hanAccessToken}}",
      test: `
pm.test("progress overview returns current streak", function () {
  const json = pm.response.json();
  pm.response.to.have.status(200);
  pm.expect(json.data.currentStreakDays).to.be.at.least(1);
});
`,
    }),
    request("18 - Delete second flashcard", "DELETE", "/flashcards/{{hanFlashcardTwoId}}", {
      auth: "{{hanAccessToken}}",
      test: `
pm.test("flashcard delete returns HTTP 204", function () { pm.response.to.have.status(204); });
`,
    }),
    request("19 - Delete Han private deck", "DELETE", "/decks/{{hanDeckId}}", {
      auth: "{{hanAccessToken}}",
      test: `
pm.test("deck delete returns HTTP 204", function () { pm.response.to.have.status(204); });
`,
    }),
  ]
);

const managerCuration = collection(
  "0003",
  "03 - Linh content manager curation",
  "Persona Linh: a content manager curates public content, approves one deck, rejects another, and verifies catalog visibility. Count: 27 Postman pm.test test cases. Requires managerEmail/managerPassword variables.",
  {
    managerEmail: "admin@example.com",
    managerPassword: "Password@123",
  },
  [
    preparePersona("linh", "/content/health", "content"),
    request("01 - Login Linh content manager", "POST", "/auth/login", {
      body: { usernameOrEmail: "{{managerEmail}}", password: "{{managerPassword}}" },
      test: `
const json = pm.response.json();
pm.test("manager login returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("manager access token is stored", function () {
  pm.collectionVariables.set("managerAccessToken", json.data.accessToken);
  pm.expect(json.data.accessToken).to.not.be.empty;
});
`,
    }),
    request("02 - Verify Linh manager role", "GET", "/auth/me", {
      auth: "{{managerAccessToken}}",
      test: `
const json = pm.response.json();
pm.test("manager current user returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("manager has CONTENT_MANAGER compatible role", function () {
  pm.expect(json.data.roles.join(",")).to.match(/CONTENT_MANAGER|ADMIN|SUPER_ADMIN/);
});
`,
    }),
    request("03 - Create curation tag", "POST", "/tags", {
      auth: "{{managerAccessToken}}",
      body: { name: "curation-{{runId}}" },
      test: `
const json = pm.response.json();
pm.test("manager can create tag with HTTP 201", function () { pm.response.to.have.status(201); });
pm.test("curation tag id is stored", function () { pm.collectionVariables.set("managerTagId", json.data.id); });
`,
    }),
    request("04 - List tags includes curation tag", "GET", "/tags", {
      test: `
pm.test("tag catalog includes manager tag", function () {
  const items = pm.response.json().data;
  const tagId = String(pm.collectionVariables.get("managerTagId"));
  pm.expect(items.some(x => String(x.id) === tagId)).to.eql(true);
});
`,
    }),
    request("05 - Create curation topic", "POST", "/topics", {
      auth: "{{managerAccessToken}}",
      body: { name: "Curation Topic {{runId}}", description: "Topic created by Linh in Postman automation" },
      test: `
const json = pm.response.json();
pm.test("manager can create topic with HTTP 201", function () { pm.response.to.have.status(201); });
pm.test("curation topic id is stored", function () { pm.collectionVariables.set("managerTopicId", json.data.id); });
`,
    }),
    request("06 - Create public deck for approval", "POST", "/decks", {
      auth: "{{managerAccessToken}}",
      body: {
        title: "Linh Approved Public Deck {{runId}}",
        description: "A public deck curated by Linh",
        topicId: "{{managerTopicId}}",
        visibility: "PUBLIC",
        tagIds: ["{{managerTagId}}"],
      },
      test: `
const json = pm.response.json();
pm.test("manager public deck create returns HTTP 201", function () { pm.response.to.have.status(201); });
pm.test("manager public deck starts as DRAFT", function () {
  pm.collectionVariables.set("managerApprovedDeckId", json.data.id);
  pm.expect(json.data.status).to.eql("DRAFT");
});
`,
    }),
    request("07 - Add curated flashcard", "POST", "/decks/{{managerApprovedDeckId}}/flashcards", {
      auth: "{{managerAccessToken}}",
      body: { frontText: "curated phrase", backText: "cum tu duoc bien tap", difficultyLevel: "MEDIUM", cardOrder: 1 },
      test: `
pm.test("manager can add flashcard with HTTP 201", function () { pm.response.to.have.status(201); });
`,
    }),
    request("08 - Submit approved deck for review", "POST", "/decks/{{managerApprovedDeckId}}/submit-review", {
      auth: "{{managerAccessToken}}",
      test: `
pm.test("submitted deck becomes PENDING", function () {
  const json = pm.response.json();
  pm.response.to.have.status(200);
  pm.expect(json.data.status).to.eql("PENDING");
});
`,
    }),
    request("09 - Approve curated deck", "POST", "/admin/decks/{{managerApprovedDeckId}}/approve", {
      auth: "{{managerAccessToken}}",
      test: `
const json = pm.response.json();
pm.test("manager approval returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("approved deck status is APPROVED", function () { pm.expect(json.data.status).to.eql("APPROVED"); });
`,
    }),
    request("10 - Public catalog shows approved deck", "GET", "/decks?keyword=Linh%20Approved&page=0&size=10", {
      test: `
const items = pm.response.json().data.items;
pm.test("approved public deck is visible anonymously", function () {
  const deckId = String(pm.collectionVariables.get("managerApprovedDeckId"));
  pm.expect(items.some(x => String(x.id) === deckId)).to.eql(true);
});
pm.test("public catalog request returns HTTP 200", function () { pm.response.to.have.status(200); });
`,
    }),
    request("11 - Create public deck for rejection", "POST", "/decks", {
      auth: "{{managerAccessToken}}",
      body: { title: "Linh Rejected Public Deck {{runId}}", description: "Rejected in automation", visibility: "PUBLIC" },
      test: `
pm.test("rejection candidate create returns HTTP 201", function () {
  const json = pm.response.json();
  pm.response.to.have.status(201);
  pm.collectionVariables.set("managerRejectedDeckId", json.data.id);
});
`,
    }),
    request("12 - Submit rejection candidate", "POST", "/decks/{{managerRejectedDeckId}}/submit-review", {
      auth: "{{managerAccessToken}}",
      test: `
pm.test("rejection candidate becomes PENDING", function () { pm.expect(pm.response.json().data.status).to.eql("PENDING"); });
`,
    }),
    request("13 - Reject curated deck candidate", "POST", "/admin/decks/{{managerRejectedDeckId}}/reject", {
      auth: "{{managerAccessToken}}",
      body: { reason: "Translations need a second review" },
      test: `
const json = pm.response.json();
pm.test("manager rejection returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("rejected deck status is REJECTED", function () { pm.expect(json.data.status).to.eql("REJECTED"); });
`,
    }),
    request("14 - Anonymous catalog hides rejected deck", "GET", "/decks?keyword=Linh%20Rejected&page=0&size=10", {
      test: `
pm.test("rejected deck is hidden from anonymous catalog", function () {
  const items = pm.response.json().data.items;
  const deckId = String(pm.collectionVariables.get("managerRejectedDeckId"));
  pm.expect(items.some(x => String(x.id) === deckId)).to.eql(false);
});
`,
    }),
    request("15 - Manager can still find rejected own deck", "GET", "/decks?keyword=Linh%20Rejected&page=0&size=10", {
      auth: "{{managerAccessToken}}",
      test: `
pm.test("manager catalog sees rejected deck", function () {
  const items = pm.response.json().data.items;
  const deckId = String(pm.collectionVariables.get("managerRejectedDeckId"));
  pm.expect(items.some(x => String(x.id) === deckId)).to.eql(true);
});
`,
    }),
    request("16 - Rework rejected deck to private draft", "PUT", "/decks/{{managerRejectedDeckId}}", {
      auth: "{{managerAccessToken}}",
      body: { title: "Linh Reworked Private Deck {{runId}}", description: "Reworked after rejection", visibility: "PRIVATE" },
      test: `
pm.test("reworked deck returns HTTP 200 and DRAFT", function () {
  const json = pm.response.json();
  pm.response.to.have.status(200);
  pm.expect(json.data.status).to.eql("DRAFT");
});
`,
    }),
    request("17 - Delete reworked rejected deck", "DELETE", "/decks/{{managerRejectedDeckId}}", {
      auth: "{{managerAccessToken}}",
      test: `
pm.test("manager delete reworked deck returns HTTP 204", function () { pm.response.to.have.status(204); });
`,
    }),
  ]
);

const adminModeration = collection(
  "0004",
  "04 - Anh admin moderation governance",
  "Persona Anh: an admin verifies governance, locks/unlocks a learner, approves content, handles reports, and checks audit logs. Count: 25 Postman pm.test test cases. Requires adminEmail/adminPassword variables.",
  {
    adminEmail: "admin@example.com",
    adminPassword: "Password@123",
  },
  [
    request("00 - Prepare admin run and confirm protected health", "GET", "/admin/health", {
      pre: `
const runId = Date.now().toString();
pm.collectionVariables.set("runId", runId);
pm.collectionVariables.set("targetEmail", "target." + runId + "@example.com");
pm.collectionVariables.set("targetUsername", "target_" + runId);
`,
      test: `
pm.test("admin health without token is protected", function () { pm.response.to.have.status(401); });
`,
    }),
    request("01 - Login Anh admin", "POST", "/auth/login", {
      body: { usernameOrEmail: "{{adminEmail}}", password: "{{adminPassword}}" },
      test: `
const json = pm.response.json();
pm.test("admin login returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("admin token is stored", function () { pm.collectionVariables.set("adminAccessToken", json.data.accessToken); });
`,
    }),
    request("02 - Admin health with token", "GET", "/admin/health", {
      auth: "{{adminAccessToken}}",
      test: `
pm.test("admin health with token returns HTTP 200", function () { pm.response.to.have.status(200); });
`,
    }),
    request("03 - Verify Anh admin role", "GET", "/auth/me", {
      auth: "{{adminAccessToken}}",
      test: `
pm.test("admin current user has admin-compatible role", function () {
  const roles = pm.response.json().data.roles.join(",");
  pm.expect(roles).to.match(/ADMIN|SUPER_ADMIN/);
});
`,
    }),
    request("04 - Read admin dashboard", "GET", "/admin/dashboard", {
      auth: "{{adminAccessToken}}",
      test: `
const json = pm.response.json();
pm.test("admin dashboard returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("admin dashboard has user and deck counts", function () {
  pm.expect(json.data.totalUsers).to.be.a("number");
  pm.expect(json.data.totalDecks).to.be.a("number");
});
`,
    }),
    request("05 - Register target learner", "POST", "/auth/register", {
      body: { email: "{{targetEmail}}", username: "{{targetUsername}}", password: "{{password}}", fullName: "Target Learner {{runId}}" },
      test: `
pm.test("target learner register returns HTTP 201", function () {
  const json = pm.response.json();
  pm.response.to.have.status(201);
  pm.collectionVariables.set("targetUserId", json.data.id);
});
`,
    }),
    request("06 - Admin lists target user", "GET", "/users?keyword={{targetUsername}}&page=0&size=10", {
      auth: "{{adminAccessToken}}",
      test: `
pm.test("user list contains target learner", function () {
  const items = pm.response.json().data.items;
  const targetUserId = String(pm.collectionVariables.get("targetUserId"));
  pm.expect(items.some(x => String(x.id) === targetUserId)).to.eql(true);
});
`,
    }),
    request("07 - Admin opens target user detail", "GET", "/users/{{targetUserId}}", {
      auth: "{{adminAccessToken}}",
      test: `
pm.test("target detail returns ACTIVE user", function () {
  const json = pm.response.json();
  pm.response.to.have.status(200);
  pm.expect(json.data.status).to.eql("ACTIVE");
});
`,
    }),
    request("08 - Admin locks target user", "POST", "/admin/users/{{targetUserId}}/lock", {
      auth: "{{adminAccessToken}}",
      test: `
const json = pm.response.json();
pm.test("lock user returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("locked user status is LOCKED", function () { pm.expect(json.data.status).to.eql("LOCKED"); });
`,
    }),
    request("09 - Locked target cannot login", "POST", "/auth/login", {
      body: { usernameOrEmail: "{{targetEmail}}", password: "{{password}}" },
      test: `
pm.test("locked target login returns HTTP 401", function () { pm.response.to.have.status(401); });
`,
    }),
    request("10 - Admin unlocks target user", "POST", "/admin/users/{{targetUserId}}/unlock", {
      auth: "{{adminAccessToken}}",
      test: `
pm.test("unlock user returns ACTIVE status", function () {
  const json = pm.response.json();
  pm.response.to.have.status(200);
  pm.expect(json.data.status).to.eql("ACTIVE");
});
`,
    }),
    request("11 - Target login works after unlock", "POST", "/auth/login", {
      body: { usernameOrEmail: "{{targetEmail}}", password: "{{password}}" },
      test: `
pm.test("unlocked target login returns HTTP 200", function () {
  const json = pm.response.json();
  pm.response.to.have.status(200);
  pm.collectionVariables.set("targetAccessToken", json.data.accessToken);
});
`,
    }),
    request("12 - Admin creates public deck to approve", "POST", "/decks", {
      auth: "{{adminAccessToken}}",
      body: { title: "Anh Admin Public Deck {{runId}}", description: "Created by admin automation", visibility: "PUBLIC" },
      test: `
pm.test("admin public deck create returns HTTP 201", function () {
  const json = pm.response.json();
  pm.response.to.have.status(201);
  pm.collectionVariables.set("adminDeckId", json.data.id);
});
`,
    }),
    request("13 - Admin submits public deck review", "POST", "/decks/{{adminDeckId}}/submit-review", {
      auth: "{{adminAccessToken}}",
      test: `
pm.test("admin deck review submission returns PENDING", function () { pm.expect(pm.response.json().data.status).to.eql("PENDING"); });
`,
    }),
    request("14 - Admin approves public deck", "POST", "/admin/decks/{{adminDeckId}}/approve", {
      auth: "{{adminAccessToken}}",
      test: `
const json = pm.response.json();
pm.test("admin approve deck returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("admin approved deck is APPROVED", function () { pm.expect(json.data.status).to.eql("APPROVED"); });
`,
    }),
    request("15 - Target reports approved deck", "POST", "/reports", {
      auth: "{{targetAccessToken}}",
      body: { targetType: "DECK", targetId: "{{adminDeckId}}", reason: "Learner asks admin to verify this deck" },
      test: `
pm.test("target report create returns HTTP 201", function () {
  const json = pm.response.json();
  pm.response.to.have.status(201);
  pm.collectionVariables.set("reportId", json.data.id);
});
`,
    }),
    request("16 - Admin lists open reports", "GET", "/admin/reports?status=OPEN&page=0&size=20", {
      auth: "{{adminAccessToken}}",
      test: `
pm.test("open report list includes target report", function () {
  const items = pm.response.json().data.items;
  const reportId = String(pm.collectionVariables.get("reportId"));
  pm.expect(items.some(x => String(x.id) === reportId)).to.eql(true);
});
`,
    }),
    request("17 - Admin resolves report", "PATCH", "/admin/reports/{{reportId}}/status", {
      auth: "{{adminAccessToken}}",
      body: { status: "RESOLVED" },
      test: `
const json = pm.response.json();
pm.test("resolve report returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("report status is RESOLVED", function () { pm.expect(json.data.status).to.eql("RESOLVED"); });
`,
    }),
    request("18 - Audit logs include deck approval", "GET", "/admin/audit-logs?action=DECK_APPROVED&resourceType=DECK&resourceId={{adminDeckId}}", {
      auth: "{{adminAccessToken}}",
      test: `
pm.test("deck approval audit log exists", function () { pm.expect(pm.response.json().data.totalItems).to.be.at.least(1); });
`,
    }),
    request("19 - Audit logs include user lock", "GET", "/admin/audit-logs?action=USER_LOCKED&resourceType=USER&resourceId={{targetUserId}}", {
      auth: "{{adminAccessToken}}",
      test: `
pm.test("user lock audit log exists", function () { pm.expect(pm.response.json().data.totalItems).to.be.at.least(1); });
`,
    }),
  ]
);

const securityBoundary = collection(
  "0005",
  "05 - Khoa security and boundary regression",
  "Persona Khoa: a QA/security tester checks unauthorized access, role boundaries, validation errors, and not-found behavior. Count: 28 Postman pm.test test cases.",
  {},
  [
    preparePersona("khoa", "/auth/health", "auth"),
    request("01 - Register Khoa learner", "POST", "/auth/register", {
      body: { email: "{{khoaEmail}}", username: "{{khoaUsername}}", password: "{{password}}", fullName: "{{khoaFullName}}" },
      test: `
pm.test("Khoa register returns HTTP 201", function () { pm.response.to.have.status(201); });
`,
    }),
    request("02 - Login Khoa learner", "POST", "/auth/login", {
      body: { usernameOrEmail: "{{khoaEmail}}", password: "{{password}}" },
      test: `
const json = pm.response.json();
pm.test("Khoa login returns HTTP 200", function () { pm.response.to.have.status(200); });
pm.test("Khoa token is stored", function () { pm.collectionVariables.set("khoaAccessToken", json.data.accessToken); });
`,
    }),
    request("03 - Admin dashboard without token", "GET", "/admin/dashboard", {
      test: `
pm.test("dashboard without token returns HTTP 401", function () { pm.response.to.have.status(401); });
`,
    }),
    request("04 - Admin dashboard with learner token", "GET", "/admin/dashboard", {
      auth: "{{khoaAccessToken}}",
      test: `
pm.test("dashboard with learner token returns HTTP 403", function () { pm.response.to.have.status(403); });
`,
    }),
    request("05 - Learner cannot create language", "POST", "/languages", {
      auth: "{{khoaAccessToken}}",
      body: { code: "k{{runIdShort}}", name: "Forbidden Language {{runId}}" },
      test: `
pm.test("learner language create returns HTTP 403", function () { pm.response.to.have.status(403); });
`,
    }),
    request("06 - Learner cannot create topic", "POST", "/topics", {
      auth: "{{khoaAccessToken}}",
      body: { name: "Forbidden Topic {{runId}}", description: "Should fail" },
      test: `
pm.test("learner topic create returns HTTP 403", function () { pm.response.to.have.status(403); });
`,
    }),
    request("07 - Learner cannot create public deck", "POST", "/decks", {
      auth: "{{khoaAccessToken}}",
      body: { title: "Forbidden Public Deck {{runId}}", visibility: "PUBLIC" },
      test: `
pm.test("learner public deck create returns HTTP 403", function () { pm.response.to.have.status(403); });
`,
    }),
    request("08 - Blank deck title validation", "POST", "/decks", {
      auth: "{{khoaAccessToken}}",
      body: { title: "", visibility: "PRIVATE" },
      test: `
const json = pm.response.json();
pm.test("blank deck title returns HTTP 400", function () { pm.response.to.have.status(400); });
pm.test("blank deck title has validation payload", function () { pm.expect(json.data).to.be.an("object"); });
`,
    }),
    request("09 - Create private deck for negative tests", "POST", "/decks", {
      auth: "{{khoaAccessToken}}",
      body: { title: "Khoa Boundary Private Deck {{runId}}", visibility: "PRIVATE" },
      test: `
pm.test("boundary private deck create returns HTTP 201", function () {
  const json = pm.response.json();
  pm.response.to.have.status(201);
  pm.collectionVariables.set("khoaDeckId", json.data.id);
});
`,
    }),
    request("10 - Private deck cannot be submitted for public review", "POST", "/decks/{{khoaDeckId}}/submit-review", {
      auth: "{{khoaAccessToken}}",
      test: `
pm.test("private submit review returns HTTP 400", function () { pm.response.to.have.status(400); });
`,
    }),
    request("11 - Unknown deck returns not found", "GET", "/decks/999999999", {
      auth: "{{khoaAccessToken}}",
      test: `
pm.test("unknown deck returns HTTP 404", function () { pm.response.to.have.status(404); });
`,
    }),
    request("12 - Blank flashcard front validation", "POST", "/decks/{{khoaDeckId}}/flashcards", {
      auth: "{{khoaAccessToken}}",
      body: { frontText: "", backText: "valid back", difficultyLevel: "EASY", cardOrder: 1 },
      test: `
const json = pm.response.json();
pm.test("blank flashcard front returns HTTP 400", function () { pm.response.to.have.status(400); });
pm.test("blank flashcard front has validation payload", function () { pm.expect(json.data).to.be.an("object"); });
`,
    }),
    request("13 - Start session with unknown deck", "POST", "/study-sessions/start", {
      auth: "{{khoaAccessToken}}",
      body: { deckId: 999999999, limit: 10 },
      test: `
pm.test("unknown deck study session returns HTTP 404", function () { pm.response.to.have.status(404); });
`,
    }),
    request("14 - Start session with invalid limit", "POST", "/study-sessions/start", {
      auth: "{{khoaAccessToken}}",
      body: { deckId: "{{khoaDeckId}}", limit: 0 },
      test: `
const json = pm.response.json();
pm.test("invalid study limit returns HTTP 400", function () { pm.response.to.have.status(400); });
pm.test("invalid study limit has validation payload", function () { pm.expect(json.data).to.be.an("object"); });
`,
    }),
    request("15 - Submit review without rating", "POST", "/reviews/999999999", {
      auth: "{{khoaAccessToken}}",
      body: { responseTimeMs: 20 },
      test: `
pm.test("missing review rating returns HTTP 400", function () { pm.response.to.have.status(400); });
`,
    }),
    request("16 - Reviews today with invalid token", "GET", "/reviews/today?limit=10", {
      auth: "bad-token",
      test: `
pm.test("bad bearer token returns HTTP 401", function () { pm.response.to.have.status(401); });
`,
    }),
    request("17 - Update missing flashcard", "PUT", "/flashcards/999999999", {
      auth: "{{khoaAccessToken}}",
      body: { frontText: "missing", backText: "missing", difficultyLevel: "EASY", cardOrder: 1 },
      test: `
pm.test("missing flashcard update returns HTTP 404", function () { pm.response.to.have.status(404); });
`,
    }),
    request("18 - Delete missing flashcard", "DELETE", "/flashcards/999999999", {
      auth: "{{khoaAccessToken}}",
      test: `
pm.test("missing flashcard delete returns HTTP 404", function () { pm.response.to.have.status(404); });
`,
    }),
    request("19 - Logout without bearer token", "POST", "/auth/logout", {
      body: { refreshToken: "not-used" },
      test: `
pm.test("logout without token returns HTTP 401", function () { pm.response.to.have.status(401); });
`,
    }),
    request("20 - Learner cannot read user detail endpoint", "GET", "/users/999999999", {
      auth: "{{khoaAccessToken}}",
      test: `
pm.test("learner user detail access returns HTTP 403", function () { pm.response.to.have.status(403); });
`,
    }),
    request("21 - Empty refresh token validation", "POST", "/auth/refresh", {
      body: { refreshToken: "" },
      test: `
const json = pm.response.json();
pm.test("empty refresh token returns HTTP 400", function () { pm.response.to.have.status(400); });
pm.test("empty refresh token has validation payload", function () { pm.expect(json.data).to.be.an("object"); });
`,
    }),
  ]
);

const environment = {
  id: uid("env1"),
  name: "Flashcard Local MongoDB",
  values: [
    { key: "baseUrl", value: "http://localhost:8080/api/v1", type: "default", enabled: true },
    { key: "managerEmail", value: "admin@example.com", type: "default", enabled: true },
    { key: "managerPassword", value: "Password@123", type: "secret", enabled: true },
    { key: "adminEmail", value: "admin@example.com", type: "default", enabled: true },
    { key: "adminPassword", value: "Password@123", type: "secret", enabled: true },
  ],
  _postman_variable_scope: "environment",
  _postman_exported_using: "Postman/Generated",
};

const readme = `# Flashcard Postman Automation Testing

Thu muc nay gom 5 Postman collection import-chay truc tiep cho backend Flashcard Learning Platform.

## Files

1. \`01_minh_learner_auth_lifecycle.postman_collection.json\`
   - Persona: Minh, learner moi.
   - Scope: register, duplicate register, login, me, refresh rotation, logout.
   - 28 \`pm.test\` test cases.

2. \`02_han_learner_private_deck_learning.postman_collection.json\`
   - Persona: Han, learner hoc hang ngay.
   - Scope: private deck, flashcards, study session, review, progress, cleanup.
   - 27 \`pm.test\` test cases.

3. \`03_linh_content_manager_curation.postman_collection.json\`
   - Persona: Linh, content manager.
   - Scope: tag/topic, public deck curation, approve/reject, catalog visibility.
   - 27 \`pm.test\` test cases.
   - Can tai khoan co role \`CONTENT_MANAGER\`, \`ADMIN\`, hoac \`SUPER_ADMIN\`.

4. \`04_anh_admin_moderation_governance.postman_collection.json\`
   - Persona: Anh, admin.
   - Scope: dashboard, user lock/unlock, deck approval, report, audit logs.
   - 25 \`pm.test\` test cases.
   - Can tai khoan co role \`ADMIN\` hoac \`SUPER_ADMIN\`.

5. \`05_khoa_security_boundary_regression.postman_collection.json\`
   - Persona: Khoa, QA/security tester.
   - Scope: 401, 403, validation 400, not found 404, invalid token.
   - 27 \`pm.test\` test cases.

Optional:

- \`flashcard_local.postman_environment.json\`: environment mau.
- \`generate-postman-collections.js\`: source generator de sua/regenerate collection.

## Cach import vao Postman

1. Mo Postman.
2. Chon \`Import\`.
3. Import 5 file \`*.postman_collection.json\`.
4. Neu muon, import them \`flashcard_local.postman_environment.json\`.
5. Chon environment \`Flashcard Local MongoDB\`.
6. Dam bao backend dang chay:

\`\`\`bash
cd backend
mvn spring-boot:run
\`\`\`

Profile mac dinh la \`dev\` voi JWT secret co san, khong can set bien moi truong them. Neu chay tren profile khac, nho set \`JWT_SECRET\`.

Base URL mac dinh:

\`\`\`text
http://localhost:8080/api/v1
\`\`\`

## Cach chay dung

- Chay tung collection bang \`Run collection\`.
- Luon chay tu request dau tien \`00 - Prepare fresh persona run\`.
- Request prepare se tao \`runId\`, email/username rieng, tranh trung data moi lan run.
- Collection 01, 02, 05 tu tao learner rieng.
- Collection 03, 04 can credential co role cao hon learner.

## Tao tai khoan admin/content manager cho collection 03 va 04

API register hien tai chi tao role \`LEARNER\`, nen ban can tao/promote user trong MongoDB local.

1. Register user bang API hoac Postman:

\`\`\`json
{
  "email": "admin@example.com",
  "username": "admin_local",
  "password": "Password@123",
  "fullName": "Admin Local"
}
\`\`\`

2. Promote trong mongosh:

\`\`\`javascript
use flashcard_platform
const adminRole = db.roles.findOne({ name: "ADMIN" })
db.users.updateOne(
  { email: "admin@example.com" },
  { $set: { roles: [DBRef("roles", adminRole._id)] } }
)
\`\`\`

Content manager:

\`\`\`javascript
use flashcard_platform
const managerRole = db.roles.findOne({ name: "CONTENT_MANAGER" })
db.users.updateOne(
  { email: "manager@example.com" },
  { $set: { roles: [DBRef("roles", managerRole._id)] } }
)
\`\`\`

3. Trong Postman, sua variables:

- \`managerEmail\`
- \`managerPassword\`
- \`adminEmail\`
- \`adminPassword\`

Co the sua trong tab \`Variables\` cua collection hoac trong environment.

## Cach sua script

Cach nhanh nhat:

1. Sua file \`generate-postman-collections.js\`.
2. Chay:

\`\`\`bash
node "automation testing/generate-postman-collections.js"
\`\`\`

3. Import lai collection JSON vao Postman.

Neu sua truc tiep trong Postman:

1. Mo collection.
2. Chon request can sua.
3. Sua tab \`Body\`, \`Authorization\`, \`Pre-request Script\`, hoac \`Tests\`.
4. Export lai collection neu muon luu ra file.

## Cac bien quan trong

- \`baseUrl\`: doi port/domain backend.
- \`runId\`: auto set moi lan chay collection tu request dau.
- \`password\`: password mac dinh cho learner auto-created.
- \`managerEmail\`, \`managerPassword\`: credential content manager.
- \`adminEmail\`, \`adminPassword\`: credential admin.

## Luu y

- Cac collection tao du lieu test tren MongoDB local.
- Collection 02 co cleanup deck/flashcard cuoi flow.
- Collection 03/04 co tao deck/report/audit log de test workflow quan tri.
- Khong can push code hay chay migration SQL.
`;

writeCollection("01_minh_learner_auth_lifecycle.postman_collection.json", learnerAuth);
writeCollection("02_han_learner_private_deck_learning.postman_collection.json", learnerStudy);
writeCollection("03_linh_content_manager_curation.postman_collection.json", managerCuration);
writeCollection("04_anh_admin_moderation_governance.postman_collection.json", adminModeration);
writeCollection("05_khoa_security_boundary_regression.postman_collection.json", securityBoundary);
fs.writeFileSync(path.join(outDir, "flashcard_local.postman_environment.json"), JSON.stringify(environment, null, 2) + "\n", "utf8");
