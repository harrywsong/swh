db = db.getSiblingDB('goals_db');

db.createCollection('goal_tracking');

print("Goals database initialized");