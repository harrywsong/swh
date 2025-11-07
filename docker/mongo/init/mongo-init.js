db = db.getSiblingDB('goals_db');

db.createCollection('goal_tracking');

db.goal_tracking.insertMany([
    {
        title: "Meditate Daily",
        description: "Practice mindfulness meditation for 10 minutes",
        targetDate: new Date("2025-12-31"),
        status: "in-progress",
        category: "mindfulness"
    },
    {
        title: "Exercise Weekly",
        description: "Complete 3 workout sessions per week",
        targetDate: new Date("2025-12-31"),
        status: "in-progress",
        category: "fitness"
    }
]);

print("Goals database initialized");