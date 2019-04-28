package codingnojutsu.clubhub

import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.util.DiffUtil
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.TextView
import com.yuyakaido.android.cardstackview.*
import java.util.*

class MainActivity : AppCompatActivity(), CardStackListener {

    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val cardStackView by lazy { findViewById<CardStackView>(R.id.card_stack_view) }
    private val manager by lazy { CardStackLayoutManager(this, this) }
    private val adapter by lazy { CardStackAdapter(createSpots()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupNavigation()
        setupCardStackView()
        setupButton()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawers()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCardDragging(direction: Direction, ratio: Float) {
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    override fun onCardSwiped(direction: Direction) {
        Log.d("CardStackView", "onCardSwiped: p = ${manager.topPosition}, d = $direction")
        if (manager.topPosition == adapter.itemCount - 5) {
            paginate()
        }
    }

    override fun onCardRewound() {
        Log.d("CardStackView", "onCardRewound: ${manager.topPosition}")
    }

    override fun onCardCanceled() {
        Log.d("CardStackView", "onCardCanceled: ${manager.topPosition}")
    }

    override fun onCardAppeared(view: View, position: Int) {
        val textView = view.findViewById<TextView>(R.id.item_name)
        Log.d("CardStackView", "onCardAppeared: ($position) ${textView.text}")
    }

    override fun onCardDisappeared(view: View, position: Int) {
        val textView = view.findViewById<TextView>(R.id.item_name)
        Log.d("CardStackView", "onCardDisappeared: ($position) ${textView.text}")
    }

    private fun setupNavigation() {
        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        // DrawerLayout
        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer)
        actionBarDrawerToggle.syncState()
        drawerLayout.addDrawerListener(actionBarDrawerToggle)

        // NavigationView
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.reload -> reload()
                R.id.add_spot_to_first -> addFirst(1)
                R.id.add_spot_to_last -> addLast(1)
                R.id.remove_spot_from_first -> removeFirst(1)
                R.id.remove_spot_from_last -> removeLast(1)
                R.id.replace_first_spot -> replace()
                R.id.swap_first_for_last -> swap()
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun setupCardStackView() {
        initialize()
    }

    private fun setupButton() {
        val skip = findViewById<View>(R.id.skip_button)
        skip.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Left)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            cardStackView.swipe()
        }

        val rewind = findViewById<View>(R.id.rewind_button)
        rewind.setOnClickListener {
            val setting = RewindAnimationSetting.Builder()
                .setDirection(Direction.Bottom)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(DecelerateInterpolator())
                .build()
            manager.setRewindAnimationSetting(setting)
            cardStackView.rewind()
        }

        val like = findViewById<View>(R.id.like_button)
        like.setOnClickListener {
            val setting = SwipeAnimationSetting.Builder()
                .setDirection(Direction.Right)
                .setDuration(Duration.Normal.duration)
                .setInterpolator(AccelerateInterpolator())
                .build()
            manager.setSwipeAnimationSetting(setting)
            cardStackView.swipe()
        }
    }

    private fun initialize() {
        manager.setStackFrom(StackFrom.None)
        manager.setVisibleCount(3)
        manager.setTranslationInterval(8.0f)
        manager.setScaleInterval(0.95f)
        manager.setSwipeThreshold(0.3f)
        manager.setMaxDegree(20.0f)
        manager.setDirections(Direction.HORIZONTAL)
        manager.setCanScrollHorizontal(true)
        manager.setCanScrollVertical(true)
        manager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager.setOverlayInterpolator(LinearInterpolator())
        cardStackView.layoutManager = manager
        cardStackView.adapter = adapter
        cardStackView.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }

    private fun paginate() {
        val old = adapter.getSpots()
        val new = old.plus(createSpots())
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun reload() {
        val old = adapter.getSpots()
        val new = createSpots()
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun addFirst(size: Int) {
        val old = adapter.getSpots()
        val new = mutableListOf<Spot>().apply {
            addAll(old)
            for (i in 0 until size) {
                add(manager.topPosition, createSpot())
            }
        }
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun addLast(size: Int) {
        val old = adapter.getSpots()
        val new = mutableListOf<Spot>().apply {
            addAll(old)
            addAll(List(size) { createSpot() })
        }
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun removeFirst(size: Int) {
        if (adapter.getSpots().isEmpty()) {
            return
        }

        val old = adapter.getSpots()
        val new = mutableListOf<Spot>().apply {
            addAll(old)
            for (i in 0 until size) {
                removeAt(manager.topPosition)
            }
        }
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun removeLast(size: Int) {
        if (adapter.getSpots().isEmpty()) {
            return
        }

        val old = adapter.getSpots()
        val new = mutableListOf<Spot>().apply {
            addAll(old)
            for (i in 0 until size) {
                removeAt(this.size - 1)
            }
        }
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun replace() {
        val old = adapter.getSpots()
        val new = mutableListOf<Spot>().apply {
            addAll(old)
            removeAt(manager.topPosition)
            add(manager.topPosition, createSpot())
        }
        adapter.setSpots(new)
        adapter.notifyItemChanged(manager.topPosition)
    }

    private fun swap() {
        val old = adapter.getSpots()
        val new = mutableListOf<Spot>().apply {
            addAll(old)
            val first = removeAt(manager.topPosition)
            val last = removeAt(this.size - 1)
            add(manager.topPosition, last)
            add(first)
        }
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter.setSpots(new)
        result.dispatchUpdatesTo(adapter)
    }

    private fun createSpot(): Spot {
        return Spot(
            name = "Squirrel Watchers",
            type = "Social",
            url = "https://media.npr.org/assets/img/2017/04/25/istock-115796521-fcf434f36d3d0865301cdcb9c996cfd80578ca99-s1100-c15.jpg"
        )
    }

    private fun createSpots(): List<Spot> {
        val spots = ArrayList<Spot>()
        spots.add(Spot(name = "Squirrel Watchers", type = "Social", url = "https://media.npr.org/assets/img/2017/04/25/istock-115796521-fcf434f36d3d0865301cdcb9c996cfd80578ca99-s1100-c15.jpg"))
        spots.add(Spot(name = "Geoff Fan Club", type = "Cult", url = "data:image/jpeg;base64,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"))
        spots.add(Spot(name = "Pre-Vet Club", type = "Professional", url = "https://vet.tufts.edu/wp-content/uploads/students-in-hospitals-sa-657-848x1024.jpg"))
        spots.add(Spot(name = "Historical European Martial Arts", type = "Sports, Historical", url = "https://assets3.thrillist.com/v1/image/1758887/size/tmg-article_default_mobile.jpg"))
        spots.add(Spot(name = "Illini Powerlifting", type = "Sports, Competitive", url = "https://se-infra-imageserver2.azureedge.net/clink/images/137d81d5-137d-4c62-868f-0a8bf6234a1eefce6bbc-a0d2-40c7-ab9f-9d8036a8e0a0.jpg?preset=w1500"))
        spots.add(Spot(name = "104°: The Illini Hot Tub Club", type = "Social", url = "https://i.pinimg.com/736x/c0/a1/ae/c0a1aeeef1ef5b553b8c3c9d15087cc8.jpg"))
        spots.add(Spot(name = "Iron Man Fan Club", type = "Social", url = "https://storage.googleapis.com/stateless-www-popaxiom-com/2019/04/02df6f97-iron-man-walk-away-from-explosions-e1554923227474.jpg"))
        spots.add(Spot(name = "Illini Star Gazers", type = "Hobby", url = "https://fsmedia.imgix.net/16/3c/54/65/d2f5/410f/9e32/784f03602f30/astronomyjpg.jpeg?rect=0%2C103%2C1920%2C959&auto=format%2Ccompress&dpr=2&w=650"))
        spots.add(Spot(name = "Falling Illini", type = "Thrilling, Social", url = "https://i1.wp.com/skysthelimit.net/wp-content/uploads/14-Things-You-Should-Know-Before-You-Go-Skydiving.jpg?fit=625%2C420&ssl=1"))
        spots.add(Spot(name = "Let's Bake This Bread", type = "Delicious", url = "https://i1.wp.com/ksmmetalfabrication.com/wp-content/uploads/2017/09/iStock-517075416-3.jpg?ssl=1"))
        return spots
    }

}
