package com.kpollman.team3;

public abstract class Issue {
    protected int id;
    protected String description;
    protected String status;
    
    public Issue(int id, String description) {
        this.id = id;
        this.description = description;
        this.status = "Open";
    }
    
    public abstract String getResolutionType();
    public abstract String getSpecialInstructions();
    
    public void setStatus(String status) { this.status = status; }
    public String getStatus() { return status; }
    public int getId() { return id; }
    public String getDescription() { return description; }
}

class EVMIssue extends Issue {
    public EVMIssue(int id, String description) { 
        super(id, description); 
    }
    public String getResolutionType() { 
        return "Technician Required"; 
    }
    public String getSpecialInstructions() { 
        return "Replace EVM immediately and contact technical team"; 
    }
}

class VoterListIssue extends Issue {
    public VoterListIssue(int id, String description) { 
        super(id, description); 
    }
    public String getResolutionType() { 
        return "Database Verification"; 
    }
    public String getSpecialInstructions() { 
        return "Check voter registration database and update records"; 
    }
}

class LawAndOrderIssue extends Issue {
    public LawAndOrderIssue(int id, String description) { 
        super(id, description); 
    }
    public String getResolutionType() { 
        return "Police Notified"; 
    }
    public String getSpecialInstructions() { 
        return "Deploy security personnel and maintain peace at booth"; 
    }
}

class AccessibilityIssue extends Issue {
    public AccessibilityIssue(int id, String description) { 
        super(id, description); 
    }
    public String getResolutionType() { 
        return "Facility Management"; 
    }
    public String getSpecialInstructions() { 
        return "Provide wheelchair access and assistive devices"; 
    }
}