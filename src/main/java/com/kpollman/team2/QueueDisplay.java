package com.kpollman.team2;

import javax.swing.JPanel;
import java.util.List;
import com.kpollman.model.QueueData;

/**
 * Polymorphism for displaying queue status in different formats (graph, table, meter).
 */
public interface QueueDisplay {
    void displayQueueStatus(JPanel container, List<QueueData> queueDataList);
}

class TableQueueDisplay implements QueueDisplay {
    @Override
    public void displayQueueStatus(JPanel container, List<QueueData> queueDataList) {
        // Logic to update a JTable with queue data
        System.out.println("Displaying queue status as Table");
    }
}

class GraphQueueDisplay implements QueueDisplay {
    @Override
    public void displayQueueStatus(JPanel container, List<QueueData> queueDataList) {
        // Logic to render a bar graph or chart representing queue lengths
        System.out.println("Displaying queue status as Graph");
    }
}

class MeterQueueDisplay implements QueueDisplay {
    @Override
    public void displayQueueStatus(JPanel container, List<QueueData> queueDataList) {
        // Logic to render a meter or gauge for each booth
        System.out.println("Displaying queue status as Meter");
    }
}
