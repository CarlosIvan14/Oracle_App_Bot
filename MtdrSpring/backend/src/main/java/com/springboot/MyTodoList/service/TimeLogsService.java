package com.springboot.MyTodoList.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.MyTodoList.model.TimeLogs;
import com.springboot.MyTodoList.repository.TimeLogsRepository;

@Service
public class TimeLogsService {

    @Autowired
    private TimeLogsRepository timeLogsRepository;

    public TimeLogs addTimeLog(TimeLogs timeLogs) {
        return timeLogsRepository.save(timeLogs);
    }

    // Is this one really usefull in here?
    public List<TimeLogs> findAllTimeLogs() {
        return timeLogsRepository.findAll();
    }

    public Optional<TimeLogs> getTimeLogById(int id) {
        return timeLogsRepository.findById(id);
    }

    public TimeLogs updateTimeLog(int id, TimeLogs timeLogDetails) {
        return timeLogsRepository.findById(id).map(timeLog -> {
            // Actualización total (PUT)
            timeLog.setStartTs(timeLogDetails.getStartTs());
            timeLog.setEndTs(timeLogDetails.getEndTs());
            
            // Guardar los cambios en el repositorio
            return timeLogsRepository.save(timeLog);
        }).orElse(null);
    }

    public TimeLogs patchTimeLog(int id, JsonNode timeLogUpdates) {
        return timeLogsRepository.findById(id).map(timeLog -> {
            ObjectMapper mapper = new ObjectMapper();
            // Para evitar fallos si se envían propiedades desconocidas
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                // Fusiona (merge) los campos enviados en taskUpdates sobre el objeto task existente
                mapper.readerForUpdating(timeLog).readValue(timeLogUpdates);
            } catch (IOException e) {
                throw new RuntimeException("Error al actualizar el timelog", e);
            }
            return timeLogsRepository.save(timeLog);
        }).orElse(null);
    }

    public boolean deleteTimeLog(int id) {
        if (timeLogsRepository.existsById(id)) {
            timeLogsRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Método para obtener todos los timeLogs de un taskAssignee.
    public List<TimeLogs> getTimeLogsByTaskAssignee(int taskAssigneeId) {
        return timeLogsRepository.findByTaskAssigneeID(taskAssigneeId);
    }

}