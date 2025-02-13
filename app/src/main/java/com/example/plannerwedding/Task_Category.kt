package com.example.plannerwedding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.database.*

class TaskCategory : Fragment() {

    private lateinit var taskPriorityContainer: LinearLayout
    private lateinit var taskWeddingVenueContainer: LinearLayout
    private lateinit var taskEntertainmentContainer: LinearLayout
    private lateinit var taskFoodContainer: LinearLayout
    private lateinit var taskCeremonyEssentialsContainer: LinearLayout
    private lateinit var taskFavorsGiftsContainer: LinearLayout
    private lateinit var taskDecorationsContainer: LinearLayout
    private lateinit var taskPhotographyContainer: LinearLayout
    private lateinit var taskHairMakeupContainer: LinearLayout
    private lateinit var taskBrideOutfitContainer: LinearLayout
    private lateinit var taskGroomOutfitContainer: LinearLayout
    private lateinit var taskTransportationContainer: LinearLayout
    private lateinit var taskOtherTasksContainer: LinearLayout

    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_task__category, container, false)

        // Initialize all the containers for categories
        taskPriorityContainer = view.findViewById(R.id.task_priorityContainer)
        taskWeddingVenueContainer = view.findViewById(R.id.task_weddingVenueContainer)
        taskEntertainmentContainer = view.findViewById(R.id.task_entertainmentContainer)
        taskFoodContainer = view.findViewById(R.id.task_foodContainer)
        taskCeremonyEssentialsContainer = view.findViewById(R.id.task_ceremonyEssentialsTaskContainer)
        taskFavorsGiftsContainer = view.findViewById(R.id.task_favorsGiftsTaskContainer)
        taskDecorationsContainer = view.findViewById(R.id.task_entertainmentTaskContainer)
        taskPhotographyContainer = view.findViewById(R.id.task_photographyContainer)
        taskHairMakeupContainer = view.findViewById(R.id.task_hairMakeupContainer)
        taskBrideOutfitContainer = view.findViewById(R.id.task_brideOutfitContainer)
        taskGroomOutfitContainer = view.findViewById(R.id.task_groomOutfitContainer)
        taskTransportationContainer = view.findViewById(R.id.task_transportationContainer)
        taskOtherTasksContainer = view.findViewById(R.id.task_otherTasksContainer)

        // Initialize Firebase database reference
        database = FirebaseDatabase.getInstance().getReference("tasks")

        // Load tasks from Firebase
        loadTasksFromFirebase()

        return view
    }

    private fun loadTasksFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear existing tasks
                clearAllTaskViews()

                // Iterate through tasks in Firebase
                for (taskSnapshot in snapshot.children) {
                    for (categorySnapshot in taskSnapshot.children) {
                        val task = categorySnapshot.getValue(Task::class.java)
                        task?.let {
                            addTaskToCategory(it)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load tasks", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun clearAllTaskViews() {
        val containers = listOf(
            taskPriorityContainer, taskWeddingVenueContainer, taskEntertainmentContainer,
            taskFoodContainer, taskCeremonyEssentialsContainer, taskFavorsGiftsContainer,
            taskDecorationsContainer, taskPhotographyContainer, taskHairMakeupContainer,
            taskBrideOutfitContainer, taskGroomOutfitContainer, taskTransportationContainer,
            taskOtherTasksContainer
        )

        for (container in containers) {
            container.removeAllViews()
        }
    }

    private fun addTaskToCategory(task: Task) {
        val containerId = when (task.category) {
            "Priority" -> taskPriorityContainer
            "Wedding Venue" -> taskWeddingVenueContainer
            "Entertainment" -> taskEntertainmentContainer
            "Food & Beverages" -> taskFoodContainer
            "Ceremony Essentials" -> taskCeremonyEssentialsContainer
            "Favors and Gifts" -> taskFavorsGiftsContainer
            "Decorations" -> taskDecorationsContainer
            "Photography and Videography" -> taskPhotographyContainer
            "Hair and Makeup" -> taskHairMakeupContainer
            "Bride's Outfit" -> taskBrideOutfitContainer
            "Groom's Outfit" -> taskGroomOutfitContainer
            "Transportation Tasks" -> taskTransportationContainer
            else -> taskOtherTasksContainer
        }

        // Inflate and bind task to category
        val taskView = layoutInflater.inflate(R.layout.fragment_task__card, containerId, false)
        val taskCard = taskView as TaskCard
        taskCard.setTask(task)

        // Add task to container
        containerId.addView(taskView)
    }

    fun removeTaskView(task: Task) {
        val containerId = when (task.category) {
            "Priority" -> taskPriorityContainer
            "Wedding Venue" -> taskWeddingVenueContainer
            "Entertainment" -> taskEntertainmentContainer
            "Food & Beverages" -> taskFoodContainer
            "Ceremony Essentials" -> taskCeremonyEssentialsContainer
            "Favors and Gifts" -> taskFavorsGiftsContainer
            "Decorations" -> taskDecorationsContainer
            "Photography and Videography" -> taskPhotographyContainer
            "Hair and Makeup" -> taskHairMakeupContainer
            "Bride's Outfit" -> taskBrideOutfitContainer
            "Groom's Outfit" -> taskGroomOutfitContainer
            "Transportation Tasks" -> taskTransportationContainer
            else -> taskOtherTasksContainer
        }

        // Find the task view by its tag (task ID)
        val taskView = containerId.findViewWithTag<View>(task.id)

        // Remove the task view from the container
        taskView?.let {
            containerId.removeView(it)
        }
    }
}

