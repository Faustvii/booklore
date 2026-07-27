import {Component, inject, OnInit} from '@angular/core';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {AppSettingsService} from '../../../shared/service/app-settings.service';
import {SettingsHelperService} from '../../../shared/service/settings-helper.service';
import {Observable} from 'rxjs';
import {AppSettings} from '../../../shared/model/app-settings.model';
import {filter, take} from 'rxjs/operators';
import {MetadataMatchWeightsComponent} from '../global-preferences/metadata-match-weights/metadata-match-weights-component';
import {TranslocoDirective, TranslocoService} from '@jsverse/transloco';

@Component({
  selector: 'app-metadata-settings-component',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    FormsModule,
    MetadataMatchWeightsComponent,
    TranslocoDirective
  ],
  templateUrl: './metadata-settings-component.html',
  styleUrl: './metadata-settings-component.scss'
})
export class MetadataSettingsComponent implements OnInit {
  private readonly appSettingsService = inject(AppSettingsService);
  private readonly settingsHelper = inject(SettingsHelperService);
  private t = inject(TranslocoService);

  readonly appSettings$: Observable<AppSettings | null> = this.appSettingsService.appSettings$;

  ngOnInit(): void {
    this.loadSettings();
  }

  private loadSettings(): void {
    this.appSettings$.pipe(
      filter((settings): settings is AppSettings => !!settings),
      take(1)
    ).subscribe({
      next: (settings) => {},
      error: (error) => {
        console.error('Failed to load settings:', error);
        this.settingsHelper.showMessage('error', this.t.translate('common.error'), this.t.translate('settingsMeta.autoDownload.loadError'));
      }
    });
  }
}