const PLANS = {
  monthly: { amountKes: 200, days: 30, label: "Rada Pro monthly" },
  annual: { amountKes: 1800, days: 365, label: "Rada Pro annual" },
};

function planFor(planId) {
  return Object.prototype.hasOwnProperty.call(PLANS, planId) ? PLANS[planId] : null;
}

module.exports = { PLANS, planFor };
