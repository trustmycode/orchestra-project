declare module 'bpmn-js' {
  interface ViewerOptions {
    container: HTMLElement | string;
  }

  export interface BpmnCanvas {
    zoom: (value: 'fit-viewport' | number) => void;
    addMarker: (elementId: string, className: string) => void;
  }

  export default class BpmnViewer {
    constructor(options: ViewerOptions);
    importXML(xml: string): Promise<{ warnings: unknown[] }>;
    get<T = unknown>(service: string): T;
    destroy(): void;
  }
}
