package com.dimovski.sportko.db.repository.repos.impl;

import android.arch.lifecycle.LiveData;
import com.dimovski.sportko.R;
import com.dimovski.sportko.db.model.Event;
import com.dimovski.sportko.db.repository.repos.EventRepository;
import com.dimovski.sportko.db.repository.repos.firestore.FirebaseEventRepository;
import com.dimovski.sportko.db.repository.repos.local.LocalEventRepositoryImpl;
import com.dimovski.sportko.internal.NoInternetConnectionEvent;
import com.dimovski.sportko.utils.FirebaseUtils;
import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.dimovski.sportko.BaseApp.getContext;
import static com.dimovski.sportko.utils.NetworkUtils.checkInternetConnection;

/**Implementation of event repository. Checks for logical conditions and calls the correct method to fetch data from online database or from local database*/
public class EventRepositoryImpl implements EventRepository {

    private FirebaseEventRepository firebaseEventRepository = FirebaseEventRepository.getInstance();
    private LocalEventRepositoryImpl localEventRepository = LocalEventRepositoryImpl.getInstance();
    private static EventRepository repository;

    public static EventRepository getInstance() {
        if (repository == null)
            repository = new EventRepositoryImpl();
        return repository;
    }


    @Override
    public LiveData<List<Event>> getUpcomingEvents() {
        return  checkInternetConnection(getContext()) ? firebaseEventRepository.getUpcomingEvents() : localEventRepository.getUpcomingEvents();
    }

    @Override
    public LiveData<List<Event>> getUpcomingEventsForCity(String city) {
        return checkInternetConnection(getContext()) ? firebaseEventRepository.getUpcomingEventsForCity(city) : localEventRepository.getMyEvents(city);
    }

    @Override
    public LiveData<List<Event>> getMyEvents(@NotNull String email) {
        return checkInternetConnection(getContext()) ? firebaseEventRepository.getMyEvents(email) : localEventRepository.getMyEvents(email);
    }


    @Override
    public long insert(Event entity) {
        if (checkInternetConnection(getContext())) {
            String id = firebaseEventRepository.insertEvent(entity);
            entity.setId(id);
            FirebaseUtils.subscribeToTopic(String.format("%s-creator", entity.getId()),true);            //subscribe user to topic in order to listen for changes in attendance
            return localEventRepository.insert(entity);
        } else {
            EventBus.getDefault().post(new NoInternetConnectionEvent(getContext().getResources().getString(R.string.cant_insert_offline)));
            return 0;
        }
    }

    @Override
    public int update(Event entity) {
        if (checkInternetConnection(getContext())) {
            firebaseEventRepository.updateEvent(entity);
            return localEventRepository.update(entity);
        } else {
            EventBus.getDefault().post(new NoInternetConnectionEvent(getContext().getResources().getString(R.string.cant_edit_offline)));
            return 0;
        }
    }

    @Override
    public int delete(Event entity) {
        if (checkInternetConnection(getContext())) {
            firebaseEventRepository.deleteEvent(entity);
            localEventRepository.delete(entity);
            return 1;
        }
        else {
            EventBus.getDefault().post(new NoInternetConnectionEvent(getContext().getResources().getString(R.string.cant_delete_offline)));
            return 0;
        }
    }

    @Override
    public LiveData<Event> get(String id) {
        if (checkInternetConnection(getContext())) {
        return firebaseEventRepository.getEvent(id);
        } else {
            return localEventRepository.get(id);
        }
    }

    @Override
    public LiveData<List<Event>> getAll() {
        return checkInternetConnection(getContext()) ? firebaseEventRepository.getAllEvents() : localEventRepository.getAll();
    }
}
