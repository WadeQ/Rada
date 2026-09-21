// Accepts 07XXXXXXXX, 01XXXXXXXX, 7XXXXXXXX, 1XXXXXXXX, +2547XXXXXXXX, 2547XXXXXXXX
// and returns 2547XXXXXXXX / 2541XXXXXXXX, or null.
function normalizeKenyanPhone(input) {
  if (typeof input !== "string") return null;
  let d = input.replace(/[\s\-()]/g, "");
  if (d.startsWith("+")) d = d.slice(1);
  if (d.startsWith("254")) d = d.slice(3);
  else if (d.startsWith("0")) d = d.slice(1);
  return /^[17]\d{8}$/.test(d) ? `254${d}` : null;
}

function maskPhone(msisdn) {
  return `${msisdn.slice(0, 5)}****${msisdn.slice(-3)}`;
}

module.exports = { normalizeKenyanPhone, maskPhone };
