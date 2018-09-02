package com.streetapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.view.ViewGroup;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import android.widget.Toast;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

//import com.streetapp.streetapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the

 * to handle interaction events.
 * Use the {@link StartPageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StartPageFragment extends Fragment {
	// TODO: Rename parameter arguments, choose names that match
	// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
	private static final String ARG_PARAM1 = "param1";
	private static final String ARG_PARAM2 = "param2";

	// TODO: Rename and change types of parameters
	private String mParam1;
	private String mParam2;
    private PopupWindow popupWindow;
    private LinearLayout linearLayout;
    private Button streetactionbutton;

//	private OnFragmentInteractionListener mListener;

	public StartPageFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param param1 Parameter 1.
	 * @param param2 Parameter 2.
	 * @return A new instance of fragment StartPageFragment.
	 */
	// TODO: Rename and change types and number of parameters
	public static StartPageFragment newInstance(String param1, String param2) {
		StartPageFragment fragment = new StartPageFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PARAM1, param1);
		args.putString(ARG_PARAM2, param2);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			mParam1 = getArguments().getString(ARG_PARAM1);
			mParam2 = getArguments().getString(ARG_PARAM2);

		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View rootView = inflater.inflate(R.layout.fragment_start_page, container, false);
		linearLayout = (LinearLayout) rootView.findViewById(R.id.linearstreetaction);
		streetactionbutton = (Button) rootView.findViewById(R.id.streetaction);
		streetactionbutton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				StreetActionPopup(v);
			}
		});
		return rootView ;
	}



    public void StreetActionPopup (View view) {
        LayoutInflater layoutInflater = (LayoutInflater) getActivity()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        View popupView = layoutInflater.inflate(R.layout.enter_post, null);
        popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, 650);
        popupWindow.setFocusable(true);
        popupWindow.update();

        TextView EnterPost = (TextView) popupView.findViewById(R.id.streetaction);

		popupWindow.showAtLocation(linearLayout, Gravity.CENTER, 0, 0);



    }



//	// TODO: Rename method, update argument and hook method into UI event
//	public void onButtonPressed(Uri uri) {
//		if (mListener != null) {
//			mListener.onFragmentInteraction(uri);
//		}
//	}

//	@Override
//	public void onAttach(Context context) {
//		super.onAttach(context);
//		if (context instanceof OnFragmentInteractionListener) {
//			mListener = (OnFragmentInteractionListener) context;
//		} else {
//			throw new RuntimeException(context.toString()
//					+ " must implement OnFragmentInteractionListener");
//		}
//	}

//	@Override
//	public void onDetach() {
//		super.onDetach();
//		mListener = null;
//	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
//	public interface OnFragmentInteractionListener {
//		// TODO: Update argument type and name
//		void onFragmentInteraction(Uri uri);
//	}
}
