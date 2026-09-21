const PLANS = {
  monthly: { amountKes: 150, days: 30, label: "Rada Pro monthly" },
  annual: { amountKes: 1200, days: 365, label: "Rada Pro annual" },
};

function planFor(planId) {
  return Object.prototype.hasOwnProperty.call(PLANS, planId) ? PLANS[planId] : null;
}

module.exports = { PLANS, planFor };
