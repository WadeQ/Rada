const BASES = {
  sandbox: "https://sandbox.safaricom.co.ke",
  production: "https://api.safaricom.co.ke",
};

function timestamp(now = new Date()) {
  const p = (n) => String(n).padStart(2, "0");
  const t = new Date(now.getTime() + 3 * 60 * 60 * 1000); // Africa/Nairobi is UTC+3, no DST
  return `${t.getUTCFullYear()}${p(t.getUTCMonth() + 1)}${p(t.getUTCDate())}${p(t.getUTCHours())}${p(t.getUTCMinutes())}${p(t.getUTCSeconds())}`;
}

function password(shortcode, passkey, ts) {
  return Buffer.from(`${shortcode}${passkey}${ts}`).toString("base64");
}

class Daraja {
  constructor({ env, consumerKey, consumerSecret, shortcode, passkey, fetchImpl = fetch }) {
    this.base = BASES[env] || BASES.sandbox;
    this.consumerKey = consumerKey;
    this.consumerSecret = consumerSecret;
    this.shortcode = shortcode;
    this.passkey = passkey;
    this.fetch = fetchImpl;
  }

  async token() {
    const basic = Buffer.from(`${this.consumerKey}:${this.consumerSecret}`).toString("base64");
    const res = await this.fetch(`${this.base}/oauth/v1/generate?grant_type=client_credentials`, {
      headers: { Authorization: `Basic ${basic}` },
    });
    if (!res.ok) throw new Error(`Daraja auth failed (${res.status})`);
    return (await res.json()).access_token;
  }

  async post(path, body) {
    const token = await this.token();
    const res = await this.fetch(`${this.base}${path}`, {
      method: "POST",
      headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });
    const json = await res.json().catch(() => ({}));
    return { ok: res.ok, status: res.status, json };
  }

  stkPush({ msisdn, amount, accountReference, description, callbackUrl }) {
    const ts = timestamp();
    return this.post("/mpesa/stkpush/v1/processrequest", {
      BusinessShortCode: this.shortcode,
      Password: password(this.shortcode, this.passkey, ts),
      Timestamp: ts,
      TransactionType: "CustomerPayBillOnline",
      Amount: amount,
      PartyA: msisdn,
      PartyB: this.shortcode,
      PhoneNumber: msisdn,
      CallBackURL: callbackUrl,
      AccountReference: accountReference,
      TransactionDesc: description,
    });
  }

  stkQuery(checkoutRequestId) {
    const ts = timestamp();
    return this.post("/mpesa/stkpushquery/v1/query", {
      BusinessShortCode: this.shortcode,
      Password: password(this.shortcode, this.passkey, ts),
      Timestamp: ts,
      CheckoutRequestID: checkoutRequestId,
    });
  }
}

module.exports = { Daraja, timestamp, password };
