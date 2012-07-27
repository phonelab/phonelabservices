//package edu.buffalo.cse.phonelab.ui;
//
//import com.darvds.ribbonmenu.RibbonMenuView;
//import com.darvds.ribbonmenu.iRibbonMenuCallback;
//
//import android.app.ActionBar.Tab;
//import android.app.ActionBar.TabListener;
//import android.app.ActionBar;
//import android.app.FragmentTransaction;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentActivity;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentPagerAdapter;
//import android.support.v4.view.ViewPager;
//import android.view.KeyEvent;
//import android.view.LayoutInflater;
//import android.view.MenuItem;
//import android.view.View;
//import android.view.ViewGroup;
//
//public class UImain extends FragmentActivity implements TabListener, iRibbonMenuCallback {
//	
//	SectionsPagerAdapter mSectionsPagerAdapter;
//	
//	ViewPager mViewPager;
//    
//    ActionBar actionBar;
//    
//    RibbonMenuView rbmView;
//
//	@Override
//	protected void onCreate(Bundle arg0) {
//		// TODO Auto-generated method stub
//		super.onCreate(arg0);
//		setContentView(R.layout.uimain);
//		
//		/*
//		 * instantiates the SectionsPagerAdapter that will return a fragment for each tab/view 
//		 */
//		
//		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
//        
//		/*
//		 * instantiates the ribbonMenuView
//		 * sets the menu listener to this activity
//		 * sets the menu items
//		 */
//		
//		rbmView = (RibbonMenuView) findViewById(R.id.ribbonMenuView);
//        rbmView.setMenuClickCallback(this);
//        rbmView.setMenuItems(R.menu.ribbomenu);
//		
//		/*
//		 * create the actionbar with tab navigation.
//		 * set the PhoneLab icon clickable
//		 */
//		
//        actionBar = getActionBar();
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//        actionBar.setHomeButtonEnabled(true);
//        
//        /*
//         * create the viewpager and sets the SectionsPagerAdapter
//         */
//        
//        mViewPager = (ViewPager) findViewById(R.id.viewpager);
//        mViewPager.setAdapter(mSectionsPagerAdapter);
//        
//        /*
//         * Sets the tabs to follow swiping through the viewpager
//         */
//        
//        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
//            @Override
//            public void onPageSelected(int position) {
//                actionBar.setSelectedNavigationItem(position);
//            }
//        });
//        
//        /*
//         * creates a tab for every viewpager view
//         * grabs the text from the adapter
//         * set the TabListener to this activity
//         */
//        
//        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
//            actionBar.addTab(
//                    actionBar.newTab()
//                            .setText(mSectionsPagerAdapter.getPageTitle(i))
//                            .setTabListener(this));
//        }
//        
//        /*
//         * set the starting tab/view to the second tab/view ("Profile")
//         */
//        mViewPager.setCurrentItem(1);
//	}
//
//	@Override
//	public void onTabReselected(Tab tab, FragmentTransaction ft) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void onTabSelected(Tab tab, FragmentTransaction ft) {
//		// TODO Auto-generated method stub
//		/*
//		 * when a tab is selected, the corresponding viewpager view is displayed
//		 */
//		mViewPager.setCurrentItem(tab.getPosition());
//	}
//
//	@Override
//	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
//		// TODO Auto-generated method stub
//		
//	}
//	
//	public class SectionsPagerAdapter extends FragmentPagerAdapter {
//
//		public SectionsPagerAdapter(FragmentManager fm) {
//			super(fm);
//			// TODO Auto-generated constructor stub
//		}
//
//		@Override
//		public Fragment getItem(int arg0) {
//			// TODO Auto-generated method stub
//			Fragment fragment = new DummySectionFragment();
//            Bundle args = new Bundle();
//            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, arg0 + 1);
//            fragment.setArguments(args);
//            return fragment;
//		}
//
//		@Override
//		public int getCount() {
//			// TODO Auto-generated method stub
//			return 4;
//		}
//
//		@Override
//		public CharSequence getPageTitle(int position) {
//			// TODO Auto-generated method stub
//			switch (position) {
//            case 0: return getString(R.string.title_section1).toUpperCase();
//            case 1: return getString(R.string.title_section2).toUpperCase();
//            case 2: return getString(R.string.title_section3).toUpperCase();
//            case 3: return getString(R.string.title_section4).toUpperCase();
//        }
//        return null;
//		}
//		
//	}
//	
//	/*
//	 * creates the dummy fragment that represents a section of the app
//	 */
//	
//	public class DummySectionFragment extends Fragment {
//		
//		public DummySectionFragment() {
//		}
//
//		public static final String ARG_SECTION_NUMBER = "section_number";
//		
//		/*
//		 * inflates each of the layouts for each viewpager view
//		 */
//		
//		@Override
//		public View onCreateView(LayoutInflater inflater, ViewGroup container,
//				Bundle savedInstanceState) {
//			// TODO Auto-generated method stub
//			Bundle args = getArguments();
//            switch (args.getInt(ARG_SECTION_NUMBER)){
//            case 1:
//            	return inflater.inflate(R.layout.myapps, container, false);
//            case 2:
//            	return inflater.inflate(R.layout.profile, container, false);
//            case 3:
//            	return inflater.inflate(R.layout.applications, container, false);
//            case 4:
//            	return inflater.inflate(R.layout.announcements, container, false);
//            }
//            return null;
//		}
//		
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// TODO Auto-generated method stub
//		int id = item.getItemId();
//		if (id == android.R.id.home) {
//			rbmView.toggleMenu();
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}
//
//	@Override
//	public boolean onKeyUp(int keyCode, KeyEvent event) {
//		// TODO Auto-generated method stub
//		if (keyCode == KeyEvent.KEYCODE_MENU) {
//	        rbmView.toggleMenu();
//	    }
//	    return true;
//	}
//
//	/*
//	 * tells the app what to do when a menu item is clicked in the RibbonMenu
//	 */
//	
//	@Override
//	public void RibbonMenuItemClick(int itemId) {
//		// TODO Auto-generated method stub
//		if (itemId == R.id.rm1) {
//		} else if (itemId == R.id.rm2) {
//		} else if (itemId == R.id.rm3) {
//		} else if (itemId == R.id.rm4) {
//		} else if (itemId == R.id.rm5) {
//		} else if (itemId == R.id.rm6) {
//		}
//	}
//
//}
