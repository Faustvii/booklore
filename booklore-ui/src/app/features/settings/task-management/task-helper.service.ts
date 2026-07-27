import {inject, Injectable} from '@angular/core';
import {MessageService} from 'primeng/api';
import {TaskCreateRequest, TaskService} from './task.service';
import {TranslocoService} from '@jsverse/transloco';

@Injectable({
  providedIn: 'root'
})
export class TaskHelperService {
  private taskService = inject(TaskService);
  private messageService = inject(MessageService);
  private readonly t = inject(TranslocoService);

  getAvailableTasks() {
    return this.taskService.getAvailableTasks();
  }

  startTask(request: TaskCreateRequest) {
    return this.taskService.startTask(request);
  }
}