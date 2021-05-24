// Copyright (c) Microsoft Corporation.
// Licensed under the MIT license.
//
// Backend implementation of the speech-verification scenario

import { MicrotaskList, IBackendScenarioInterface } from '../ScenarioInterface';
import {
  baseSpeechVerificationScenario,
  BaseSpeechVerificationScenario,
  Microtask,
  SpeechVerificationTaskRecord,
} from '@karya/core';
import { Promise as BBPromise } from 'bluebird';
import { promises as fsp } from 'fs';

/**
 * Process the input files for the speech verification task.
 * @param task Speech verification task record
 * @param jsonFilePath Path to JSON file
 * @param tarFilePath Path to tar ball
 * @param task_folder Task folder path
 */
async function processInputFile(
  task: SpeechVerificationTaskRecord,
  jsonData?: any,
  tarFilePath?: string,
  task_folder?: string
): Promise<MicrotaskList> {
  // Get all the objects from the json Data
  const verifications: { sentence: string; recording: string }[] = jsonData!;

  // Extract the microtasks
  const microtasks = await BBPromise.mapSeries(verifications, async ({ sentence, recording }) => {
    const filePath = `${task_folder}/${recording}`;
    try {
      await fsp.access(filePath);

      const microtask: Microtask = {
        task_id: task.id,
        input: {
          data: { sentence },
          files: { recording },
        },
        deadline: task.deadline,
        credits: task.params.creditsPerVerification,
        status: 'INCOMPLETE',
      };

      return microtask;
    } catch (e) {
      throw new Error(`Recording file not present`);
    }
  });

  return [{ mg: null, microtasks }];
}

// Backend speech verification scenario
export const backendSpeechVerificationScenario: IBackendScenarioInterface<BaseSpeechVerificationScenario> = {
  ...baseSpeechVerificationScenario,
  processInputFile,
};