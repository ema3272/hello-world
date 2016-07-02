package be.ema.sclibrary;


public class ScMain extends AppCompatActivity implements View.OnClickListener, FragmentManager.OnBackStackChangedListener {

    public static Toolbar scToolbar;
    public static ActionBar actionBar;
    public static FloatingActionButton fab = null;
    public static Menu overflowMenu;
    public static ScSpinner spinner;
    public static MenuItem eraseItem;
    public static MenuItem isInitializedMenuItem;
    static ScTutorial tutorialFragment;

    public static String AB_STEP;
    public static String AB_CONFIRM;
    public static String AB_UNDO;
    public static String AB_ABORT;

    final Runnable BackgroundInitialisation = new Runnable() {
        public void run() {
            initializeSC();
        }
    };

    public void initializeSC() {
        initLocalizedBtnLbl();
    }

    public void initLocalizedBtnLbl() {
        AB_STEP = getString(R.string.ActionBarStep);
        AB_CONFIRM = getString(R.string.ActionBarConfirm);
        AB_UNDO = getString(R.string.ActionBarUndo);
        AB_ABORT = getString(R.string.ActionBarAbort);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isBeingRestored = (savedInstanceState != null);
        mContext = this;

        setContentView(R.layout.activity_main);

        fragmentManager = getFragmentManager();

        scToolbar = (Toolbar) findViewById(R.id.Sc_toolbar);
        setSupportActionBar(scToolbar);

        scToolbar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scToolbar.inflateMenu(R.menu.menu_main);
                //Remove the listener before proceeding
                scToolbar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                View menu = getMenuItemView(mToolbar, R.id.menuDelete); //this works too but quite unnecessary
                View menuItem = findViewById(R.id.AbErase);
                if (menuItem != null) {
                    int[] location = new int[2];
                    menuItem.getLocationOnScreen(location);
                    //coordinates of the exact center
                    int centerX = location[0] + (menuItem.getWidth()/2);
                    int centerY = location[1] + (menuItem.getHeight()/2);
                }
            }
        });

        scToolbar.post(new Runnable() {
            @Override
            public void run() {
                if (!isBeingRestored) {
                    if (IS_DEBUG) {  //isolate change in progress in case the production version has to be recompiled
                        scToolbar.inflateMenu(R.menu.menu_main);
                        View menuItem = findViewById(R.id.AbErase);
                        if (menuItem != null) {
                            int[] location = new int[2];
                            menuItem.getLocationOnScreen(location);
                            //coordinates of the exact center
                            int centerX = location[0] + (menuItem.getWidth()/2);
                            int centerY = location[1] + (menuItem.getHeight()/2);
                        }

                        View v = findViewById(R.id.helpFrameLayout);
                        v.setVisibility(View.VISIBLE);


                        tutorialFragment = new ScTutorial();

                        // Add the fragment to the 'fragment_container' FrameLayout
                        fragmentManager.beginTransaction()
                                .add(R.id.helpFrameLayout, tutorialFragment)
                                .addToBackStack(null)   // Add this transaction to the back stack
                                .commit();
                    } else {
                        DialogFragment newFragment = new ScWelcome();
                        newFragment.show(getFragmentManager(), "dialog");
                    }
                }
            }
        });

        // Get a support ActionBar corresponding to this toolbar
        actionBar = getSupportActionBar();
        actionBar.setIcon((fullOrLite == 0) ? R.mipmap.icon_full : R.mipmap.icon_lite);
        actionBar.setDisplayShowHomeEnabled(true);

        //Listen for changes in the back stack
        fragmentManager.addOnBackStackChangedListener(this);
        //Handle when activity is recreated like on orientation Change
        shouldDisplayHomeUp();

        // Inflate your custom layout
        final ViewGroup actionBarLayout = (ViewGroup) getLayoutInflater().inflate(
                R.layout.strategy_spinner, null);
        // Set up your ActionBar
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(actionBarLayout);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.select_sc_type, STRATEGY_NAMES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner = (ScSpinner) findViewById(R.id.strategy_spinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                resetActionPerformed(true, false);

                if (position == (STRATEGY_NAMES.length - 1)) {
                } else {
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                return;
            }
        });

        isLandscapeOriented = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        if (isLandscapeOriented) {

            // Calculate ActionBar height
            int actionBarHeight = 0;
            TypedValue tv = new TypedValue();
            if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
            }

        }

        scMainHandler.post(BackgroundInitialisation);
    }

    public void shouldDisplayHomeUp() {
        //Enable Up button only  if there are entries in the back stack
//        boolean canback = fragmentManager.getBackStackEntryCount()>0;
        if (fragmentManager.getBackStackEntryCount() > 0) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        } else {
            View v = findViewById(R.id.helpFrameLayout);
            v.setVisibility(View.GONE);
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    @Override
    public void onBackPressed() {
        if (currentScSetting == SETTING_GESTURE) {
            fab.show();
        }

        if (fragmentManager.getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            fragmentManager.popBackStack();
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        actionBar.collapseActionView();

        overflowMenu = menu;

        isInitializedMenuItem = menu.findItem(R.id.AbInitialized);
        isInitializedMenuItem.setChecked(isInitializeCbxChecked);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        eraseItem = menu.findItem(R.id.AbErase);


        item = menu.findItem(R.id.AbInitialized);
        item.setChecked(isInitializeCbxChecked);

        item = menu.findItem(R.id.AbStep);
        Drawable fabIcon = fab.getDrawable();
        item.setIcon(fabIcon);
        if (fabIcon == OK_ICON) {
            item.setTitle(AB_CONFIRM);
        } else if (fabIcon == STEP_ICON) {
            item.setTitle(AB_STEP);
        }

        return true;
    }
}