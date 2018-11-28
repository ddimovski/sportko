package com.dimovski.sportko.ui;


import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.bumptech.glide.Glide;
import com.dimovski.sportko.R;
import com.dimovski.sportko.data.Constants;
import com.dimovski.sportko.db.model.Event;
import com.dimovski.sportko.db.repository.FirebaseRepository;
import com.dimovski.sportko.utils.DateTimeUtils;
import com.dimovski.sportko.viewmodel.EventDetailViewModel;
import com.dimovski.sportko.viewmodel.EventViewModel;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class EventDetailActivity extends BaseActivity implements View.OnClickListener {

    Unbinder unbinder;
    FirebaseRepository repository = new FirebaseRepository();
    EventDetailViewModel viewModel;

    @BindView(R.id.event_photo_edit)
    ImageView photo;
    @BindView(R.id.event_title)
    TextView title;
    @BindView(R.id.event_desc)
    TextView description;
    @BindView(R.id.event_type)
    TextView type;
    @BindView(R.id.event_date)
    TextView date;
    @BindView(R.id.attending)
    TextView attending;
    @BindView(R.id.attendEvent)
    Button attendEvent;
    @BindView(R.id.where)
    TextView location;
    @BindView(R.id.attendingEvent)
    Button attendingEvent;

    Event event;
    int eventId;
    SharedPreferences sharedPreferences;
    String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        unbinder = ButterKnife.bind(this);

        viewModel = ViewModelProviders.of(this).get(EventDetailViewModel.class);

        attendEvent.setOnClickListener(this);
        attendingEvent.setOnClickListener(this);
        sharedPreferences = getSharedPreferences(Constants.SHARED_PREF,MODE_PRIVATE);
        currentUser = sharedPreferences.getString(Constants.EMAIL,"");
    }


    @Override
    protected void onResume() {
        super.onResume();
        viewModel.getEvent(event.getId()).observe(this, new Observer<Event>() {
            @Override
            public void onChanged(@Nullable Event event) {
                if (event!=null){
                    initUi(event);
                }

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        viewModel.getEvent(event.getId()).removeObservers(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Event event) {
        this.event = event;
        EventBus.getDefault().removeStickyEvent(event);
        initUi(event);
    }

    private void initUi(Event event) {
        this.event=event;
        title.setText(event.getTitle());
        description.setText(event.getDescription());
        type.setText(String.format("Type: %s", event.getTypeOfEvent()));
        Glide.with(this).load(event.getImgSrc()).into(photo);
        date.setText(String.format("%s %s", DateTimeUtils.formatDate(event.getScheduled(), this), DateTimeUtils.formatTime(event.getScheduled(), this)));
        int noOfAtendees = event.getAttendees().size();
        attending.setText(String.format("%d / %d", noOfAtendees, event.getMaxAttendees()));
        location.setText(event.getLocationName());

        if (event.getAttendees().indexOf(currentUser)>-1) {
            setAttending(true);
        } else setAttending(false);
    }

    private void setAttending(boolean shouldAttend) {
            if (shouldAttend) {
                attendingEvent.setVisibility(View.VISIBLE);
                attendEvent.setVisibility(View.GONE);
            } else {
                attendingEvent.setVisibility(View.GONE);
                attendEvent.setVisibility(View.VISIBLE);
            }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.attendEvent:
                if (event.getAttendees().indexOf(currentUser)<0) {

                    if (event.getAttendees().size() < event.getMaxAttendees()) {
                        event.addAttendee(currentUser);
                        setAttending(true);
                        repository.updateEvent(event);
                    } else {
                        Toast.makeText(this, R.string.cannot_attend_full_event, Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.attendingEvent:
                if (event.getAttendees().indexOf(currentUser)>=0) {
                    event.removeAtendee(currentUser);
                    setAttending(false);
                    repository.updateEvent(event);
                }
        }
    }
}
