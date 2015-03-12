# RecyclerGesture
First attempt to easily attached simple gesture to recycle view based on RecycleView.addOnItemTouchListener()

# Contribution
PR are welcomed (= !

Try to fit the current naming convention.
Ensure that gradlew :library:check succeeded before submitting any PR.

# Restrictions
Currently work with fixedSize item in the recycler view.

```java

    recyclerView.setHasFixedSize(true);

```

Currently don't work with LinearLayout as RecyclerView parent since dragging view is added to the
parent view hierarchy. Works fine with RelativeLayout/FrameLayout.

#Disclaimer
Under development, check restrictions above.

Based on previous work of ismoli :
https://github.com/ismoli/DynamicRecyclerView

