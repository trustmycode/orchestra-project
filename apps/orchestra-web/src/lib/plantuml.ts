export const encodePlantUml = (text: string): string => {
  const str = unescape(encodeURIComponent(text));
  let hex = '';
  for (let i = 0; i < str.length; i++) {
    hex += str.charCodeAt(i).toString(16).padStart(2, '0');
  }
  return `~h${hex}`;
};

export const getPlantUmlServerUrl = (): string => {
  return 'http://localhost:8095';
};
